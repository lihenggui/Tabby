package com.github.kr328.clash.settings.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.ConfigurationOverride
import org.jetbrains.compose.resources.stringResource
import tabby.ui.settings.generated.resources.Res
import tabby.ui.settings.generated.resources.geofile_import_failed
import tabby.ui.settings.generated.resources.geofile_imported
import tabby.ui.settings.generated.resources.geofile_unknown_db_format
import tabby.ui.settings.generated.resources.geofile_unknown_db_format_message
import tabby.ui.settings.generated.resources.ok

@Composable
fun MetaFeatureSettingsRouteContent(
  onResetCompleted: () -> Unit,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  initialConfiguration: ConfigurationOverride = metaFeatureSettingsInitialConfiguration(),
  geoFileImportResult: GeoFileImportResult = GeoFileImportResult.Idle,
  onConfigurationChange: (ConfigurationOverride) -> Unit = {},
  onReset: () -> Unit = {},
  onImportGeoIp: () -> Unit = {},
  onImportGeoSite: () -> Unit = {},
  onImportCountry: () -> Unit = {},
  onImportASN: () -> Unit = {},
) {
  val configurationState = remember { mutableStateOf(initialConfiguration) }
  val geoFileImportDisplayState = remember { mutableStateOf(geoFileImportInitialDisplayState()) }
  val currentOnConfigurationChange = rememberUpdatedState(onConfigurationChange)
  val currentOnReset = rememberUpdatedState(onReset)
  val geoFileImportedText =
    (geoFileImportResult as? GeoFileImportResult.Success)?.let {
      stringResource(Res.string.geofile_imported, it.displayName)
    }
  val geoFileImportFailedText = stringResource(Res.string.geofile_import_failed)

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

  LaunchedEffect(geoFileImportResult, geoFileImportedText, geoFileImportFailedText) {
    when (val action = geoFileImportResultDisplayAction(geoFileImportResult)) {
      is GeoFileImportResultDisplayAction.ShowImported -> {
        snackbarHostState.showSnackbar(message = geoFileImportedText ?: action.displayName)
      }
      is GeoFileImportResultDisplayAction.ShowUnsupportedFormat -> {
        geoFileImportDisplayState.value =
          updateGeoFileImportDisplayStateForAction(geoFileImportDisplayState.value, action)
      }
      GeoFileImportResultDisplayAction.ShowFailed ->
        snackbarHostState.showSnackbar(message = geoFileImportFailedText)
      GeoFileImportResultDisplayAction.Ignore -> Unit
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

  val currentGeoFileImportDisplayState = geoFileImportDisplayState.value

  if (currentGeoFileImportDisplayState.showUnsupportedFormatDialog) {
    AlertDialog(
      onDismissRequest = {
        geoFileImportDisplayState.value =
          dismissGeoFileImportUnsupportedFormatDialog(geoFileImportDisplayState.value)
      },
      title = { Text(stringResource(Res.string.geofile_unknown_db_format)) },
      text = {
        Text(
          stringResource(
            Res.string.geofile_unknown_db_format_message,
            currentGeoFileImportDisplayState.unsupportedFormatSummary,
          )
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            geoFileImportDisplayState.value =
              dismissGeoFileImportUnsupportedFormatDialog(geoFileImportDisplayState.value)
          }
        ) {
          Text(text = stringResource(Res.string.ok))
        }
      },
    )
  }
}
