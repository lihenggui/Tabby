package com.github.kr328.clash.settings.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.ConfigurationOverride

@Composable
fun MetaFeatureSettingsRouteContent(
  onResetCompleted: () -> Unit,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  initialConfiguration: ConfigurationOverride = metaFeatureSettingsInitialConfiguration(),
  onConfigurationChange: (ConfigurationOverride) -> Unit = {},
  onReset: () -> Unit = {},
  onImportGeoIp: () -> Unit = {},
  onImportGeoSite: () -> Unit = {},
  onImportCountry: () -> Unit = {},
  onImportASN: () -> Unit = {},
) {
  val configurationState = remember { mutableStateOf(initialConfiguration) }
  val currentOnConfigurationChange = rememberUpdatedState(onConfigurationChange)
  val currentOnReset = rememberUpdatedState(onReset)

  fun updateConfiguration(transform: (ConfigurationOverride) -> ConfigurationOverride) {
    val updated = transform(configurationState.value)
    configurationState.value = updated
    currentOnConfigurationChange.value(updated)
  }

  val actions = remember {
    object : MetaFeatureSettingsActions {
      override fun updateUnifiedDelay(value: Boolean?) = updateConfiguration {
        updateMetaUnifiedDelay(it, value)
      }

      override fun updateGeodataMode(value: Boolean?) = updateConfiguration {
        updateMetaGeodataMode(it, value)
      }

      override fun updateTcpConcurrent(value: Boolean?) = updateConfiguration {
        updateMetaTcpConcurrent(it, value)
      }

      override fun updateFindProcessMode(value: ConfigurationOverride.FindProcessMode?) =
        updateConfiguration {
          updateMetaFindProcessMode(it, value)
        }

      override fun updateSnifferEnable(value: Boolean?) = updateConfiguration {
        updateMetaSnifferEnable(it, value)
      }

      override fun updateSniffHttpPorts(value: List<String>?) = updateConfiguration {
        updateSniffProtocolPorts(it, SniffProtocol.Http, value)
      }

      override fun updateSniffHttpOverrideDestination(value: Boolean?) = updateConfiguration {
        updateSniffProtocolOverrideDestination(it, SniffProtocol.Http, value)
      }

      override fun updateSniffTlsPorts(value: List<String>?) = updateConfiguration {
        updateSniffProtocolPorts(it, SniffProtocol.Tls, value)
      }

      override fun updateSniffTlsOverrideDestination(value: Boolean?) = updateConfiguration {
        updateSniffProtocolOverrideDestination(it, SniffProtocol.Tls, value)
      }

      override fun updateSniffQuicPorts(value: List<String>?) = updateConfiguration {
        updateSniffProtocolPorts(it, SniffProtocol.Quic, value)
      }

      override fun updateSniffQuicOverrideDestination(value: Boolean?) = updateConfiguration {
        updateSniffProtocolOverrideDestination(it, SniffProtocol.Quic, value)
      }

      override fun updateForceDnsMapping(value: Boolean?) = updateConfiguration {
        updateMetaSnifferForceDnsMapping(it, value)
      }

      override fun updateParsePureIp(value: Boolean?) = updateConfiguration {
        updateMetaSnifferParsePureIp(it, value)
      }

      override fun updateOverrideDestination(value: Boolean?) = updateConfiguration {
        updateMetaSnifferOverrideDestination(it, value)
      }

      override fun updateForceDomain(value: List<String>?) = updateConfiguration {
        updateMetaSnifferForceDomain(it, value)
      }

      override fun updateSkipDomain(value: List<String>?) = updateConfiguration {
        updateMetaSnifferSkipDomain(it, value)
      }

      override fun updateSkipSrcAddress(value: List<String>?) = updateConfiguration {
        updateMetaSnifferSkipSrcAddress(it, value)
      }

      override fun updateSkipDstAddress(value: List<String>?) = updateConfiguration {
        updateMetaSnifferSkipDstAddress(it, value)
      }
    }
  }

  LaunchedEffect(initialConfiguration) {
    if (configurationState.value != initialConfiguration) {
      configurationState.value = initialConfiguration
    }
  }

  MetaFeatureSettingsNavigatorContent { onOpenEditableTextList ->
    val showResetConfirmDialog = remember { mutableStateOf(false) }

    MetaFeatureSettingsContent(
      configuration = configurationState.value,
      actions = actions,
      snackbarHostState = snackbarHostState,
      modifier = modifier,
      showResetConfirmDialog = showResetConfirmDialog.value,
      onShowResetConfirmDialogChange = { showResetConfirmDialog.value = it },
      onResetConfirmed = {
        val resetConfiguration = metaFeatureSettingsInitialConfiguration()
        configurationState.value = resetConfiguration
        currentOnConfigurationChange.value(resetConfiguration)
        currentOnReset.value()
        onResetCompleted()
      },
      onImportGeoIp = onImportGeoIp,
      onImportGeoSite = onImportGeoSite,
      onImportCountry = onImportCountry,
      onImportASN = onImportASN,
      onOpenEditableTextList = onOpenEditableTextList,
    )
  }
}
