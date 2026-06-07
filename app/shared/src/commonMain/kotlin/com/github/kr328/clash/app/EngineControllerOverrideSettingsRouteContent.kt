package com.github.kr328.clash.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.engine.api.EngineController
import com.github.kr328.clash.settings.ui.OverridePersistAction
import com.github.kr328.clash.settings.ui.OverrideSettingsRouteContent
import com.github.kr328.clash.settings.ui.overridePersistAction
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.withContext

@Composable
internal fun EngineControllerOverrideSettingsRouteContent(
  engineController: EngineController,
  onResetCompleted: () -> Unit,
  modifier: Modifier = Modifier,
  onActionError: (Throwable) -> Unit = {},
) {
  EngineControllerPersistedOverrideRouteContent(
    engineController = engineController,
    onActionError = onActionError,
  ) { configuration, onConfigurationChange, onReset ->
    OverrideSettingsRouteContent(
      onResetCompleted = onResetCompleted,
      modifier = modifier,
      initialConfiguration = configuration,
      onConfigurationChange = onConfigurationChange,
      onReset = onReset,
    )
  }
}

@Composable
internal fun EngineControllerPersistedOverrideRouteContent(
  engineController: EngineController,
  onActionError: (Throwable) -> Unit = {},
  content: @Composable (ConfigurationOverride, (ConfigurationOverride) -> Unit, () -> Unit) -> Unit,
) {
  var configuration by remember(engineController) { mutableStateOf(ConfigurationOverride()) }
  var resetRequested by remember(engineController) { mutableStateOf(false) }
  val currentConfiguration = rememberUpdatedState(configuration)
  val currentResetRequested = rememberUpdatedState(resetRequested)
  val currentOnActionError = rememberUpdatedState(onActionError)

  LaunchedEffect(engineController) {
    var queryCompleted = false

    try {
      configuration = engineController.queryPersistOverride()
      queryCompleted = true
      awaitCancellation()
    } catch (cause: Throwable) {
      if (cause is CancellationException) throw cause

      currentOnActionError.value(cause)
    } finally {
      if (queryCompleted) {
        withContext(NonCancellable) {
          runCatching {
              engineController.persistOverrideSettings(
                overridePersistAction(
                  skipPersist = currentResetRequested.value,
                  configuration = currentConfiguration.value,
                )
              )
            }
            .onFailure(currentOnActionError.value)
        }
      }
    }
  }

  content(
    configuration,
    { value ->
      configuration = value
      resetRequested = false
    },
    { resetRequested = true },
  )
}

private suspend fun EngineController.persistOverrideSettings(action: OverridePersistAction) {
  when (action) {
    OverridePersistAction.Clear -> clearPersistOverride()
    is OverridePersistAction.Patch -> patchPersistOverride(action.configuration)
  }
}
