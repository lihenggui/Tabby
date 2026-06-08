package com.github.kr328.clash.settings.ui

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.engine.android.AndroidEngineController
import com.github.kr328.clash.glue.util.clashDir
import com.github.kr328.clash.settings.R
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun MetaFeatureSettingsScreen(
  modifier: Modifier = Modifier,
  onResetCompleted: () -> Unit,
) {
  val context = LocalContext.current
  val appContext = context.applicationContext
  val engineController = remember(appContext) { AndroidEngineController(appContext) }
  val importScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  val importedText = stringResource(R.string.geofile_imported)
  val importFailedText = stringResource(R.string.geofile_import_failed)
  var importResult by remember { mutableStateOf(geoFileImportInitialResult()) }
  var pendingImportType by remember { mutableStateOf<GeoFileImportType?>(null) }
  var importDisplayState by remember { mutableStateOf(geoFileImportInitialDisplayState()) }

  LaunchedEffect(importResult) {
    when (val action = geoFileImportResultDisplayAction(importResult)) {
      is GeoFileImportResultDisplayAction.ShowImported -> {
        snackbarHostState.showSnackbar(message = importedText.format(action.displayName))
      }
      is GeoFileImportResultDisplayAction.ShowUnsupportedFormat -> {
        importDisplayState = updateGeoFileImportDisplayStateForAction(importDisplayState, action)
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
          importScope.launch {
            importResult = geoFileImportStartedResult()
            importResult = appContext.importGeoFile(action.source, action.importType)
          }
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

  EngineControllerMetaFeatureSettingsRouteContent(
    engineController = engineController,
    onResetCompleted = onResetCompleted,
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    onImportGeoIp = { requestGeoFileImport(GeoFileImportType.GeoIp) },
    onImportGeoSite = { requestGeoFileImport(GeoFileImportType.GeoSite) },
    onImportCountry = { requestGeoFileImport(GeoFileImportType.Country) },
    onImportASN = { requestGeoFileImport(GeoFileImportType.ASN) },
  )

  if (importDisplayState.showUnsupportedFormatDialog) {
    AlertDialog(
      onDismissRequest = {
        importDisplayState = dismissGeoFileImportUnsupportedFormatDialog(importDisplayState)
      },
      title = { Text(stringResource(R.string.geofile_unknown_db_format)) },
      text = {
        Text(
          stringResource(
            R.string.geofile_unknown_db_format_message,
            importDisplayState.unsupportedFormatSummary,
          )
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            importDisplayState = dismissGeoFileImportUnsupportedFormatDialog(importDisplayState)
          }
        ) {
          Text(text = stringResource(CommonR.string.ok))
        }
      },
    )
  }
}

private suspend fun Context.importGeoFile(
  uri: Uri?,
  importType: GeoFileImportType,
): GeoFileImportResult =
  withContext(Dispatchers.IO) {
    try {
      val sourceUri = uri ?: return@withContext geoFileImportFailedResult()
      val cursor =
        contentResolver.query(sourceUri, null, null, null, null, null)
          ?: return@withContext geoFileImportFailedResult()

      cursor.use {
        val sourceReadable = it.moveToFirst()
        val displayName = if (sourceReadable) it.displayName else null
        when (
          val sourceAction =
            geoFileImportSourceActionFromPlatformSource(
              source = sourceUri,
              sourceReadable = sourceReadable,
              displayName = displayName,
              importType = importType,
            )
        ) {
          GeoFileImportSourceAction.Fail -> geoFileImportFailedResult()
          is GeoFileImportSourceAction.Import -> {
            when (val action = sourceAction.action) {
              is GeoFileImportAction.UnsupportedFormat -> geoFileImportResult(action)
              is GeoFileImportAction.Copy -> {
                val outputFile = clashDir.resolve(action.outputFileName)
                outputFile.parentFile?.mkdirs()
                val inputStream =
                  contentResolver.openInputStream(sourceUri)
                    ?: return@use geoFileImportResult(action, copySucceeded = false)
                inputStream.use { ins ->
                  outputFile.outputStream().use { outs -> ins.copyTo(outs) }
                }
                geoFileImportResult(action, copySucceeded = true)
              }
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.e("Import geo database failed: ${e.message}", e)
      geoFileImportFailedResult()
    }
  }

private val Cursor.displayName: String?
  get() {
    val columnIndex = getColumnIndex(OpenableColumns.DISPLAY_NAME)
    return if (columnIndex != -1) getString(columnIndex) else null
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
