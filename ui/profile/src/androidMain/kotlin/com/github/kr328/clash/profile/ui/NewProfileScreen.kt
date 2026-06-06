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
    rememberLauncherForActivityResult(ScanQRCode()) { result -> viewModel.onQRResult(result) }

  val externalProviderLauncher =
    rememberLauncherForActivityResult(StartActivityForResult()) { result ->
      val uri = result.data?.data
      val action =
        newProfileExternalProviderResultAction(
          resultAccepted = result.resultCode == RESULT_OK,
          sourceSelected = uri != null,
          name = result.data?.getStringExtra(Intents.EXTRA_NAME),
        )
      when (action) {
        is NewProfileExternalProviderResultAction.CreateProfile ->
          viewModel.onExternalProviderResult(checkNotNull(uri), action.name)
        NewProfileExternalProviderResultAction.Ignore -> Unit
      }
    }

  LaunchedEffect(eventState) {
    when (val event = eventState) {
      NewProfileEventState.Idle -> Unit
      NewProfileEventState.LaunchQRScanner -> qrLauncher.launch(null)
      is NewProfileEventState.LaunchExternalProvider ->
        externalProviderLauncher.launch(event.externalProvider)
      is NewProfileEventState.LaunchProperties -> onProperties(event.uuid)
      is NewProfileEventState.OpenAppSettings ->
        context.startActivity(
          Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(event.target)
        )
      is NewProfileEventState.ShowMessage -> snackbarHostState.showSnackbar(message = event.message)
      NewProfileEventState.Finish -> onFinish()
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
  return NewProfileProviderItem(
    name = name,
    summary = summary,
    iconPainter = rememberProfileProviderPainter(icon),
    hasDetail = this is ProfileProvider.External,
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
