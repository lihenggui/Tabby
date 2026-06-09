package com.github.kr328.clash.profile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.engine.android.AndroidProfileRepository
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.MutableSharedFlow

@Composable
internal fun PropertiesScreen(
  uuid: Uuid,
  modifier: Modifier = Modifier,
  onBrowseFiles: (Uuid) -> Unit,
  onFinish: (Boolean) -> Unit,
) {
  val profileRepository = remember { AndroidProfileRepository() }
  val autoSaveEvents = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }
  val lifecycleOwner = LocalLifecycleOwner.current

  DisposableEffect(lifecycleOwner, autoSaveEvents) {
    val observer = LifecycleEventObserver { _, event ->
      if (
        propertiesAutoSaveRequestedFromStopEvent(isStopEvent = event == Lifecycle.Event.ON_STOP)
      ) {
        autoSaveEvents.tryEmit(Unit)
      }
    }

    lifecycleOwner.lifecycle.addObserver(observer)

    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  ProfileRepositoryPropertiesRouteContent(
    profileRepository = profileRepository,
    uuid = uuid,
    onBrowseFiles = onBrowseFiles,
    onFinish = onFinish,
    modifier = modifier,
    autoSaveEvents = autoSaveEvents,
    onActionError = { cause -> Log.e("Profile properties action failed: ${cause.message}", cause) },
  )
}

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun PropertiesContentPreview() {
  PropertiesStateRouteContent(
    state =
      propertiesInitialUiState()
        .withLoadedProfile(
          Profile(
            uuid = Uuid.fromLongs(0, 0),
            name = "Meta Profile",
            type = Profile.Type.Url,
            source = "https://example.com/config.yaml",
            active = false,
            interval = 60.minutes.inWholeMilliseconds,
            upload = 0,
            download = 0,
            total = 0,
            expire = 0,
            updatedAt = 0,
            imported = false,
            pending = false,
          )
        ),
    showExitWithoutSavingDialog = false,
    onBack = {},
    onDismissExitWithoutSavingDialog = {},
    onBrowseFiles = {},
    onCommit = {},
    onRequestClose = {},
    onNameChanged = {},
    onUrlChanged = {},
    onIntervalChanged = {},
  )
}
