package com.github.kr328.clash.settings.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.github.kr328.clash.engine.api.EngineController

@Composable
fun EngineControllerMetaFeatureSettingsRouteContent(
  engineController: EngineController,
  onResetCompleted: () -> Unit,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  onActionError: (Throwable) -> Unit = {},
  onImportGeoIp: () -> Unit = {},
  onImportGeoSite: () -> Unit = {},
  onImportCountry: () -> Unit = {},
  onImportASN: () -> Unit = {},
) {
  EngineControllerPersistedOverrideRouteContent(
    engineController = engineController,
    onActionError = onActionError,
  ) { configuration, onConfigurationChange, onReset ->
    MetaFeatureSettingsRouteContent(
      onResetCompleted = onResetCompleted,
      modifier = modifier,
      snackbarHostState = snackbarHostState,
      initialConfiguration = configuration,
      onConfigurationChange = onConfigurationChange,
      onReset = onReset,
      onImportGeoIp = onImportGeoIp,
      onImportGeoSite = onImportGeoSite,
      onImportCountry = onImportCountry,
      onImportASN = onImportASN,
    )
  }
}
