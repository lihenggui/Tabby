package com.github.kr328.clash.settings.ui

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.engine.android.AndroidEngineController
import com.github.kr328.clash.glue.util.clashDir
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
  var importResult by remember { mutableStateOf(geoFileImportInitialResult()) }
  var pendingImportType by remember { mutableStateOf<GeoFileImportType?>(null) }

  val importLauncher =
    rememberLauncherForActivityResult(GetContent()) { uri ->
      when (val action = geoFileImportPickerResultAction(pendingImportType, uri)) {
        is GeoFileImportPickerResultAction.Import -> {
          pendingImportType = null
          importScope.launch {
            importResult = geoFileImportStartedResult()
            importResult = appContext.importGeoFile(action.source, action.importType)
          }
        }
        GeoFileImportPickerResultAction.Fail -> {
          pendingImportType = null
          importResult = geoFileImportFailedResult()
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
    geoFileImportResult = importResult,
    onImportGeoIp = { requestGeoFileImport(GeoFileImportType.GeoIp) },
    onImportGeoSite = { requestGeoFileImport(GeoFileImportType.GeoSite) },
    onImportCountry = { requestGeoFileImport(GeoFileImportType.Country) },
    onImportASN = { requestGeoFileImport(GeoFileImportType.ASN) },
  )
}

private suspend fun Context.importGeoFile(
  uri: Uri,
  importType: GeoFileImportType,
): GeoFileImportResult =
  withContext(Dispatchers.IO) {
    try {
      when (val sourceAction = readAndroidGeoFileImportSourceAction(uri, importType)) {
        AndroidGeoFileImportSourceAction.Fail -> geoFileImportFailedResult()
        is AndroidGeoFileImportSourceAction.Import -> {
          when (val action = sourceAction.action) {
            is GeoFileImportAction.UnsupportedFormat -> geoFileImportResult(action)
            is GeoFileImportAction.Copy -> {
              val outputFile = clashDir.resolve(action.outputFileName)
              outputFile.parentFile?.mkdirs()
              val inputStream =
                contentResolver.openInputStream(sourceAction.source)
                  ?: return@withContext geoFileImportResult(action, copySucceeded = false)
              inputStream.use { ins ->
                outputFile.outputStream().use { outs -> ins.copyTo(outs) }
              }
              geoFileImportResult(action, copySucceeded = true)
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.e("Import geo database failed: ${e.message}", e)
      geoFileImportFailedResult()
    }
  }

private sealed interface AndroidGeoFileImportSourceAction {
  data class Import(val source: Uri, val action: GeoFileImportAction) :
    AndroidGeoFileImportSourceAction

  data object Fail : AndroidGeoFileImportSourceAction
}

private fun Context.readAndroidGeoFileImportSourceAction(
  uri: Uri,
  importType: GeoFileImportType,
): AndroidGeoFileImportSourceAction {
  val cursor =
    contentResolver.query(uri, null, null, null, null, null)
      ?: return AndroidGeoFileImportSourceAction.Fail

  cursor.use {
    val sourceReadable = it.moveToFirst()
    return when (
      val action =
        geoFileImportSourceActionFromPlatformState(
          sourceAvailable = true,
          sourceReadable = sourceReadable,
          displayName = { it.displayName },
          importType = importType,
        )
    ) {
      GeoFileImportSourceAction.Fail -> AndroidGeoFileImportSourceAction.Fail
      is GeoFileImportSourceAction.Import ->
        AndroidGeoFileImportSourceAction.Import(source = uri, action = action.action)
    }
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
