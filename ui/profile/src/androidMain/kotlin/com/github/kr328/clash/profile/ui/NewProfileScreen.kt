package com.github.kr328.clash.profile.ui

import android.app.Activity.RESULT_OK
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource as androidStringResource
import androidx.core.graphics.drawable.toBitmap
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.engine.android.AndroidProfileRepository
import com.github.kr328.clash.profile.R
import com.github.kr328.clash.ui.theme.tabbyDimens
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import kotlin.math.roundToInt
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class AndroidExternalProfileProvider(
  val componentKey: String?,
  val packageName: String?,
  val name: String,
  val summary: String,
  val icon: Drawable?,
  val launchTarget: Intent,
)

@Composable
internal fun NewProfileScreen(
  modifier: Modifier = Modifier,
  onProperties: (Uuid) -> Unit,
) {
  val context = LocalContext.current
  val profileRepository = remember { AndroidProfileRepository() }
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val createRequests = remember { MutableSharedFlow<NewProfileCreateRequest>() }
  var externalProviders by remember { mutableStateOf(emptyList<AndroidExternalProfileProvider>()) }
  val externalProvidersByKey =
    remember(externalProviders) { externalProviders.associateBy { it.key } }
  val missingPermissionMessage = androidStringResource(R.string.import_from_qr_no_permission)
  val scanErrorMessage = androidStringResource(R.string.import_from_qr_exception)

  fun launchCreateRequest(request: NewProfileCreateRequest) {
    scope.launch { createRequests.emit(request) }
  }

  fun showQrMessage(message: String) {
    scope.launch { snackbarHostState.showSnackbar(message) }
  }

  val qrLauncher =
    rememberLauncherForActivityResult(ScanQRCode()) { result ->
      when (
        val action =
          newProfileQrScanAction(
            result = result.toProfileQrScanResult(),
            missingPermissionMessage = missingPermissionMessage,
            scanErrorMessage = scanErrorMessage,
          )
      ) {
        is NewProfileQrScanAction.CreateProfile -> launchCreateRequest(action.request)
        is NewProfileQrScanAction.ShowMessage -> showQrMessage(action.message)
        NewProfileQrScanAction.Ignore -> Unit
      }
    }

  val externalProviderLauncher =
    rememberLauncherForActivityResult(StartActivityForResult()) { result ->
      val request =
        newProfileCreateRequestFromExternalProviderResult(
          result = result.toNewProfileExternalProviderResult(),
          sourceText = Uri::toString,
        )

      request?.let(::launchCreateRequest)
    }

  LaunchedEffect(context) {
    externalProviders = loadExternalProfileProviders(context)
  }

  ProfileRepositoryNewProfileRouteContent(
    profileRepository = profileRepository,
    onProperties = onProperties,
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    createRequests = createRequests,
    externalProviders = externalProviders.map { it.toNewProfileRouteExternalProvider() },
    onLaunchQrScanner = { qrLauncher.launch(null) },
    onCreateExternal = { provider ->
      externalProvidersByKey[provider.key]?.let { externalProvider ->
        externalProviderLauncher.launch(externalProvider.launchTarget)
      }
    },
    onDetailExternal = { provider ->
      externalProvidersByKey[provider.key]?.openAppSettings(context::startActivity)
    },
    onActionError = { cause -> Log.e("Create profile failed: ${cause.message}", cause) },
  )
}

private suspend fun loadExternalProfileProviders(
  context: Context
): List<AndroidExternalProfileProvider> {
  val appContext = context.applicationContext
  val packageManager = appContext.packageManager

  return withContext(Dispatchers.IO) {
    packageManager.queryIntentActivities(Intent(Intents.ACTION_PROVIDE_URL), 0).map {
      val activity = it.activityInfo
      val name = activity.applicationInfo.loadLabel(packageManager)
      val summary = activity.loadLabel(packageManager)
      val icon = activity.loadIcon(packageManager)
      val intent =
        Intent(Intents.ACTION_PROVIDE_URL)
          .setComponent(ComponentName(activity.packageName, activity.name))

      AndroidExternalProfileProvider(
        componentKey = intent.component?.flattenToString(),
        packageName = intent.component?.packageName,
        name = name.toString(),
        summary = summary.toString(),
        icon = icon,
        launchTarget = intent,
      )
    }
  }
}

@Composable
private fun AndroidExternalProfileProvider.toNewProfileRouteExternalProvider():
  NewProfileRouteExternalProvider {
  val presentation =
    NewProfileExternalProviderPresentation(
      key = key,
      name = name,
      summary = summary,
      hasDetail = hasDetail,
    )

  return NewProfileRouteExternalProvider(
    key = presentation.key,
    name = presentation.name,
    summary = presentation.summary,
    iconPainter = rememberProfileProviderPainter(icon),
    hasDetail = presentation.hasDetail,
  )
}

@Composable
private fun rememberProfileProviderPainter(iconDrawable: Drawable?): Painter? {
  val density = LocalDensity.current
  val headerSize = tabbyDimens.itemHeaderComponentSize
  val iconSizePx = with(density) { headerSize.toPx().roundToInt() }

  return remember(iconDrawable, iconSizePx) {
    iconDrawable
      ?.toBitmap(width = iconSizePx, height = iconSizePx)
      ?.asImageBitmap()
      ?.let(::BitmapPainter)
  }
}

private fun QRResult.toProfileQrScanResult(): ProfileQrScanResult {
  return when (this) {
    is QRSuccess ->
      ProfileQrScanResult(
        kind = ProfileQrResultKind.Success,
        rawValue = content.rawValue,
        rawBytes = content.rawBytes,
      )
    QRUserCanceled -> ProfileQrScanResult(kind = ProfileQrResultKind.UserCanceled)
    QRMissingPermission -> ProfileQrScanResult(kind = ProfileQrResultKind.MissingPermission)
    is QRError -> ProfileQrScanResult(kind = ProfileQrResultKind.Error)
  }
}

private val AndroidExternalProfileProvider.key: String
  get() = componentKey ?: packageName ?: name

private val AndroidExternalProfileProvider.hasDetail: Boolean
  get() = newProfileDetailAction(packageName) != NewProfileDetailAction.Ignore

private fun ActivityResult.toNewProfileExternalProviderResult():
  NewProfileExternalProviderResult<Uri> {
  val resultAccepted = resultCode == RESULT_OK
  val source = if (resultAccepted) data?.data else null

  return NewProfileExternalProviderResult(
    resultAccepted = resultAccepted,
    source = source,
    name = if (source != null) data?.getStringExtra(Intents.EXTRA_NAME) else null,
  )
}

private fun AndroidExternalProfileProvider.openAppSettings(startActivity: (Intent) -> Unit) {
  when (val action = newProfileDetailAction(packageName)) {
    is NewProfileDetailAction.OpenAppSettings ->
      startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
          .setData(Uri.fromParts("package", action.packageName, null))
      )
    NewProfileDetailAction.Ignore -> Unit
  }
}
