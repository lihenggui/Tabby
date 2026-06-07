package com.github.kr328.clash.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.kr328.clash.engine.api.EngineController
import com.github.kr328.clash.settings.ui.MetaFeatureSettingsRouteContent

@Composable
internal fun EngineControllerMetaFeatureSettingsRouteContent(
  engineController: EngineController,
  onResetCompleted: () -> Unit,
  modifier: Modifier = Modifier,
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
