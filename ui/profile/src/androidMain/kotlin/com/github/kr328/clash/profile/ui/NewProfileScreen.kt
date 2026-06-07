package com.github.kr328.clash.profile.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.profile.model.ProfileProvider
import com.github.kr328.clash.profile.vm.NewProfileViewModel
import com.github.kr328.clash.ui.theme.tabbyDimens
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import kotlin.math.roundToInt
import kotlin.uuid.Uuid

@Composable
internal fun NewProfileScreen(
  modifier: Modifier = Modifier,
  viewModel: NewProfileViewModel = viewModel(),
  onProperties: (Uuid) -> Unit,
  onFinish: () -> Unit,
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val eventState by viewModel.eventState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  val qrLauncher =
    rememberLauncherForActivityResult(ScanQRCode()) { result ->
      viewModel.onQRResult(result.toProfileQrScanResult())
    }

  val externalProviderLauncher =
    rememberLauncherForActivityResult(StartActivityForResult()) { result ->
      val uri = result.data?.data
      val action =
        newProfileExternalProviderResultAction(
          newProfileExternalProviderResultFromPlatformPayload(
            resultAccepted =
              newProfileExternalProviderResultAcceptedFromPlatformResultCode(
                resultCode = result.resultCode,
                acceptedResultCode = RESULT_OK,
              ),
            sourceSelected = uri != null,
            name = result.data?.getStringExtra(Intents.EXTRA_NAME),
          )
        )
      when (action) {
        is NewProfileExternalProviderResultAction.CreateProfile ->
          viewModel.onExternalProviderResult(checkNotNull(uri), action.name)
        NewProfileExternalProviderResultAction.Ignore -> Unit
      }
    }

  LaunchedEffect(eventState) {
    when (val action = newProfileEventPlatformAction(eventState)) {
      NewProfileEventPlatformAction.Ignore -> Unit
      NewProfileEventPlatformAction.LaunchQRScanner -> qrLauncher.launch(null)
      is NewProfileEventPlatformAction.LaunchExternalProvider ->
        externalProviderLauncher.launch(action.externalProvider)
      is NewProfileEventPlatformAction.LaunchProperties -> onProperties(action.uuid)
      is NewProfileEventPlatformAction.OpenAppSettings ->
        context.startActivity(
          Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(action.target)
        )
      is NewProfileEventPlatformAction.ShowMessage ->
        snackbarHostState.showSnackbar(message = action.message)
      NewProfileEventPlatformAction.Finish -> onFinish()
    }
    viewModel.consumeEvent()
  }

  val providers = uiState.providers
  NewProfileContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    providers = providers.map { it.toNewProfileProviderItem() },
    onCreate = { index ->
      when (val action = newProfileProviderSelectionAction(providers, index)) {
        is NewProfileProviderSelectionAction.SelectProvider -> viewModel.onCreate(action.provider)
        NewProfileProviderSelectionAction.Ignore -> Unit
      }
    },
    onDetail = { index ->
      when (
        val action =
          newProfileProviderDetailSelectionAction(providers, index) { provider ->
            provider as? ProfileProvider.External
          }
      ) {
        is NewProfileProviderSelectionAction.SelectProvider -> viewModel.onDetail(action.provider)
        NewProfileProviderSelectionAction.Ignore -> Unit
      }
    },
  )
}

@Composable
private fun ProfileProvider.toNewProfileProviderItem(): NewProfileProviderItem {
  return newProfileProviderItem(
    name = name,
    summary = summary,
    iconPainter = rememberProfileProviderPainter(icon),
    kind = kind,
  )
}

@Composable
private fun rememberProfileProviderPainter(icon: Any?): Painter? {
  val density = LocalDensity.current
  val headerSize = tabbyDimens.itemHeaderComponentSize
  val iconSizePx = with(density) { headerSize.toPx().roundToInt() }

  val iconVector = icon as? ImageVector
  if (iconVector != null) {
    return rememberVectorPainter(iconVector)
  }

  val iconDrawable = icon as? Drawable
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
      profileQrScanResultFromPlatformPayload(
        kind = ProfileQrResultKind.Success,
        rawValue = content.rawValue,
        rawBytes = content.rawBytes,
      )
    QRUserCanceled ->
      profileQrScanResultFromPlatformPayload(kind = ProfileQrResultKind.UserCanceled)
    QRMissingPermission ->
      profileQrScanResultFromPlatformPayload(kind = ProfileQrResultKind.MissingPermission)
    is QRError -> profileQrScanResultFromPlatformPayload(kind = ProfileQrResultKind.Error)
  }
}
