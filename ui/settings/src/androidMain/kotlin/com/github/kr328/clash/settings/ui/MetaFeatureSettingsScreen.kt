package com.github.kr328.clash.settings.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.settings.R
import com.github.kr328.clash.settings.vm.MetaFeatureSettingsViewModel
import com.github.kr328.clash.ui.lifecycle.viewModelWithLifecycle
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper

@Composable
internal fun MetaFeatureSettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: MetaFeatureSettingsViewModel = viewModelWithLifecycle(),
  onResetCompleted: () -> Unit,
) {
  val configuration by viewModel.configuration.collectAsStateWithLifecycle()
  val importResult by viewModel.importResult.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val importedText = stringResource(R.string.geofile_imported)
  val importFailedText = stringResource(R.string.geofile_import_failed)
  var pendingImportType by remember { mutableStateOf<GeoFileImportType?>(null) }
  var showUnsupportedFormatDialog by remember { mutableStateOf(false) }
  var validExtensionsSummary by remember { mutableStateOf("") }

  LaunchedEffect(importResult) {
    when (val action = geoFileImportResultDisplayAction(importResult)) {
      is GeoFileImportResultDisplayAction.ShowImported -> {
        snackbarHostState.showSnackbar(message = importedText.format(action.displayName))
      }
      is GeoFileImportResultDisplayAction.ShowUnsupportedFormat -> {
        validExtensionsSummary = action.summary
        showUnsupportedFormatDialog = true
      }
      GeoFileImportResultDisplayAction.ShowFailed ->
        snackbarHostState.showSnackbar(message = importFailedText)
      GeoFileImportResultDisplayAction.Ignore -> Unit
    }
  }

  val importLauncher =
    rememberLauncherForActivityResult(GetContent()) { uri ->
      when (
        val action =
          geoFileImportPickerResultAction(
            geoFileImportPickerResultFromPlatformPayload(
              source = uri,
              pendingImportType = pendingImportType,
            )
          )
      ) {
        is GeoFileImportPickerResultAction.Import -> {
          pendingImportType = null
          viewModel.importGeoFile(action.source, action.importType)
        }
        GeoFileImportPickerResultAction.Ignore -> Unit
      }
    }

  fun requestGeoFileImport(importType: GeoFileImportType?) {
    when (val action = geoFileImportRequestAction(importType)) {
      is GeoFileImportRequestAction.RequestPicker -> {
        pendingImportType = action.importType
        importLauncher.launch("*/*")
      }
      GeoFileImportRequestAction.Ignore -> Unit
    }
  }

  MetaFeatureSettingsRouteContent(
    onResetCompleted = onResetCompleted,
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    initialConfiguration = configuration,
    onConfigurationChange = viewModel::setConfiguration,
    onReset = viewModel::resetOverride,
    onImportGeoIp = { requestGeoFileImport(GeoFileImportType.GeoIp) },
    onImportGeoSite = { requestGeoFileImport(GeoFileImportType.GeoSite) },
    onImportCountry = { requestGeoFileImport(GeoFileImportType.Country) },
    onImportASN = { requestGeoFileImport(GeoFileImportType.ASN) },
  )

  if (showUnsupportedFormatDialog) {
    AlertDialog(
      onDismissRequest = { showUnsupportedFormatDialog = false },
      title = { Text(stringResource(R.string.geofile_unknown_db_format)) },
      text = {
        Text(stringResource(R.string.geofile_unknown_db_format_message, validExtensionsSummary))
      },
      confirmButton = {
        TextButton(onClick = { showUnsupportedFormatDialog = false }) {
          Text(text = stringResource(CommonR.string.ok))
        }
      },
    )
  }
}

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun MetaFeatureSettingsContentPreview() {
  MetaFeatureSettingsRouteContent(
    onResetCompleted = {},
    initialConfiguration = ConfigurationOverride(),
  )
}
