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
import androidx.core.graphics.drawable.toBitmap
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.engine.android.AndroidProfileRepository
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
  val scope = rememberCoroutineScope()
  val createRequests = remember { MutableSharedFlow<NewProfileCreateRequest>() }
  val qrScanResults = remember { MutableSharedFlow<ProfileQrScanResult>() }
  var externalProviders by remember { mutableStateOf(emptyList<AndroidExternalProfileProvider>()) }
  val externalProvidersByKey =
    remember(externalProviders) {
      externalProviders.associateBy { it.toNewProfileExternalProviderPresentation().key }
    }

  fun launchCreateRequest(request: NewProfileCreateRequest) {
    scope.launch { createRequests.emit(request) }
  }

  fun launchQrScanResult(result: ProfileQrScanResult) {
    scope.launch { qrScanResults.emit(result) }
  }

  val qrLauncher =
    rememberLauncherForActivityResult(ScanQRCode()) { result ->
      launchQrScanResult(result.toProfileQrScanResult())
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
    createRequests = createRequests,
    qrScanResults = qrScanResults,
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
  val presentation = toNewProfileExternalProviderPresentation()

  return newProfileRouteExternalProviderFromPresentation(
    presentation = presentation,
    iconPainter = rememberProfileProviderPainter(icon),
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
      profileQrScanResultFromSource(
        kind = ProfileQrScanSourceResultKind.Success,
        rawValue = content.rawValue,
        rawBytes = content.rawBytes,
      )
    QRUserCanceled ->
      profileQrScanResultFromSource(kind = ProfileQrScanSourceResultKind.UserCanceled)
    QRMissingPermission ->
      profileQrScanResultFromSource(kind = ProfileQrScanSourceResultKind.MissingPermission)
    is QRError -> profileQrScanResultFromSource(kind = ProfileQrScanSourceResultKind.Error)
  }
}

private fun AndroidExternalProfileProvider.toNewProfileExternalProviderPresentation():
  NewProfileExternalProviderPresentation =
  newProfileExternalProviderPresentationFromPlatformPayload(
    provider = this,
    componentKey = AndroidExternalProfileProvider::componentKey,
    packageName = AndroidExternalProfileProvider::packageName,
    name = AndroidExternalProfileProvider::name,
    summary = AndroidExternalProfileProvider::summary,
  )

private fun ActivityResult.toNewProfileExternalProviderResult():
  NewProfileExternalProviderResult<Uri> =
  newProfileExternalProviderResultFromPlatformResult(
    resultCode = resultCode,
    acceptedResultCode = RESULT_OK,
    source = data?.data,
    name = { data?.getStringExtra(Intents.EXTRA_NAME) },
  )

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
