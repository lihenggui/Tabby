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
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.settings.R
import com.github.kr328.clash.settings.vm.MetaFeatureSettingsViewModel
import com.github.kr328.clash.ui.lifecycle.viewModelWithLifecycle
import com.github.kr328.clash.ui.nav.TabbyNavDisplay
import com.github.kr328.clash.ui.nav.addIfNotLast
import com.github.kr328.clash.ui.nav.rememberNavBackStackBuilder
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper
import kotlinx.serialization.Serializable

private sealed interface MetaFeatureSettingsRoute : NavKey {
  @Serializable data object Main : MetaFeatureSettingsRoute
}

@Composable
internal fun MetaFeatureSettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: MetaFeatureSettingsViewModel = viewModelWithLifecycle(),
  onResetCompleted: () -> Unit,
) {
  val backStack = rememberNavBackStackBuilder { add(MetaFeatureSettingsRoute.Main) }
  var currentEditableTextListOnApply by remember {
    mutableStateOf<((List<String>?) -> Unit)?>(null)
  }

  TabbyNavDisplay(
    backStack = backStack,
    entryProvider =
      entryProvider {
        entry<MetaFeatureSettingsRoute.Main> {
          val configuration by viewModel.configuration.collectAsStateWithLifecycle()
          val importResult by viewModel.importResult.collectAsStateWithLifecycle()
          val snackbarHostState = remember { SnackbarHostState() }
          val importedText = stringResource(R.string.geofile_imported)
          val importFailedText = stringResource(R.string.geofile_import_failed)
          var pendingImportType by remember { mutableStateOf<GeoFileImportType?>(null) }
          var showUnsupportedFormatDialog by remember { mutableStateOf(false) }
          var validExtensionsSummary by remember { mutableStateOf("") }
          var showResetConfirmDialog by remember { mutableStateOf(false) }

          LaunchedEffect(importResult) {
            when (val result = importResult) {
              Idle,
              InProgress -> Unit

              is Success -> {
                snackbarHostState.showSnackbar(message = importedText.format(result.displayName))
              }

              is UnsupportedFormat -> {
                validExtensionsSummary = result.summary
                showUnsupportedFormatDialog = true
              }

              Failed -> snackbarHostState.showSnackbar(message = importFailedText)
            }
          }

          val importLauncher =
            rememberLauncherForActivityResult(GetContent()) { uri ->
              val type = pendingImportType ?: return@rememberLauncherForActivityResult
              pendingImportType = null
              viewModel.importGeoFile(uri, type)
            }

          MetaFeatureSettingsContent(
            configuration = configuration,
            actions = viewModel,
            snackbarHostState = snackbarHostState,
            modifier = modifier,
            showResetConfirmDialog = showResetConfirmDialog,
            onShowResetConfirmDialogChange = { showResetConfirmDialog = it },
            onResetConfirmed = {
              viewModel.resetOverride()
              onResetCompleted()
            },
            onImportGeoIp = {
              pendingImportType = GeoFileImportType.GeoIp
              importLauncher.launch("*/*")
            },
            onImportGeoSite = {
              pendingImportType = GeoFileImportType.GeoSite
              importLauncher.launch("*/*")
            },
            onImportCountry = {
              pendingImportType = GeoFileImportType.Country
              importLauncher.launch("*/*")
            },
            onImportASN = {
              pendingImportType = GeoFileImportType.ASN
              importLauncher.launch("*/*")
            },
            onOpenEditableTextList = { title, initialValues, onApply ->
              currentEditableTextListOnApply = onApply
              backStack.addIfNotLast(EditableTextList(title, initialValues?.toSet()))
            },
          )

          if (showUnsupportedFormatDialog) {
            AlertDialog(
              onDismissRequest = { showUnsupportedFormatDialog = false },
              title = { Text(stringResource(R.string.geofile_unknown_db_format)) },
              text = {
                Text(
                  stringResource(R.string.geofile_unknown_db_format_message, validExtensionsSummary)
                )
              },
              confirmButton = {
                TextButton(onClick = { showUnsupportedFormatDialog = false }) {
                  Text(text = stringResource(CommonR.string.ok))
                }
              },
            )
          }
        }
        editableTextSetScreenEntry(
          onDismiss = {
            currentEditableTextListOnApply = null
            backStack.removeLastOrNull()
          },
          onApply = { newValues ->
            currentEditableTextListOnApply?.invoke(newValues?.toList())
            currentEditableTextListOnApply = null
            backStack.removeLastOrNull()
          },
        )
      },
  )
}

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun MetaFeatureSettingsContentPreview() {
  MetaFeatureSettingsContent(
    configuration = ConfigurationOverride(),
    actions = object : MetaFeatureSettingsActions {},
    snackbarHostState = SnackbarHostState(),
    showResetConfirmDialog = false,
    onShowResetConfirmDialogChange = {},
    onResetConfirmed = {},
    onImportGeoIp = {},
    onImportGeoSite = {},
    onImportCountry = {},
    onImportASN = {},
    onOpenEditableTextList = { _, _, _ -> },
  )
}
