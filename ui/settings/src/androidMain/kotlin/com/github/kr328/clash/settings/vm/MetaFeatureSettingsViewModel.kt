package com.github.kr328.clash.settings.vm

import android.app.Application
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.engine.android.AndroidEngineController
import com.github.kr328.clash.engine.api.EngineController
import com.github.kr328.clash.glue.util.clashDir
import com.github.kr328.clash.settings.ui.GeoFileImportAction
import com.github.kr328.clash.settings.ui.GeoFileImportResult
import com.github.kr328.clash.settings.ui.GeoFileImportSourceAction
import com.github.kr328.clash.settings.ui.GeoFileImportType
import com.github.kr328.clash.settings.ui.OverridePersistAction
import com.github.kr328.clash.settings.ui.geoFileImportFailedResult
import com.github.kr328.clash.settings.ui.geoFileImportInitialResult
import com.github.kr328.clash.settings.ui.geoFileImportResult
import com.github.kr328.clash.settings.ui.geoFileImportSourceAction
import com.github.kr328.clash.settings.ui.geoFileImportStartedResult
import com.github.kr328.clash.settings.ui.metaFeatureSettingsInitialConfiguration
import com.github.kr328.clash.settings.ui.overridePersistAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class MetaFeatureSettingsViewModel(app: Application) :
  AndroidViewModel(app), DefaultLifecycleObserver {
  private val engineController: EngineController = AndroidEngineController(app)
  private val appContext = app
  @Volatile private var skipPersist = false

  val configuration: StateFlow<ConfigurationOverride>
    field = MutableStateFlow(metaFeatureSettingsInitialConfiguration())

  val importResult: StateFlow<GeoFileImportResult>
    field = MutableStateFlow(geoFileImportInitialResult())

  init {
    viewModelScope.launch {
      configuration.value = engineController.queryPersistOverride()
    }
  }

  override fun onStop(owner: LifecycleOwner) {
    // Intended to use non-viewModel scope as we need the action to be called on disposed.
    Global.launch {
      when (val action = overridePersistAction(skipPersist, configuration.value)) {
        OverridePersistAction.Clear -> engineController.clearPersistOverride()
        is OverridePersistAction.Patch ->
          engineController.patchPersistOverride(action.configuration)
      }
    }
  }

  fun resetOverride() {
    skipPersist = true
  }

  fun setConfiguration(value: ConfigurationOverride) {
    skipPersist = false
    configuration.value = value
  }

  fun importGeoFile(uri: Uri?, importType: GeoFileImportType) {
    viewModelScope.launch(Dispatchers.IO) {
      importResult.value = geoFileImportStartedResult()
      try {
        val resolver = appContext.contentResolver
        val sourceUri =
          uri
            ?: run {
              importResult.value = geoFileImportFailedResult()
              return@launch
            }
        val cursor: Cursor =
          resolver.query(sourceUri, null, null, null, null, null)
            ?: run {
              importResult.value = geoFileImportFailedResult()
              return@launch
            }

        importResult.value = cursor.use {
          val sourceReadable = it.moveToFirst()
          val displayName = if (sourceReadable) it.displayName else null
          when (
            val sourceAction =
              geoFileImportSourceAction(
                sourceSelected = true,
                sourceReadable = sourceReadable,
                displayName = displayName,
                importType = importType,
              )
          ) {
            GeoFileImportSourceAction.Fail -> return@use geoFileImportFailedResult()
            is GeoFileImportSourceAction.Import -> {
              when (val action = sourceAction.action) {
                is GeoFileImportAction.UnsupportedFormat -> return@use geoFileImportResult(action)
                is GeoFileImportAction.Copy -> {
                  val outputFile = appContext.clashDir.resolve(action.outputFileName)
                  outputFile.parentFile?.mkdirs()
                  val inputStream =
                    resolver.openInputStream(sourceUri)
                      ?: return@use geoFileImportResult(action, copySucceeded = false)
                  inputStream.use { ins ->
                    outputFile.outputStream().use { outs -> ins.copyTo(outs) }
                  }
                  return@use geoFileImportResult(action, copySucceeded = true)
                }
              }
            }
          }
        }
      } catch (e: Exception) {
        Log.e("Import geo database failed: ${e.message}", e)
        importResult.value = geoFileImportFailedResult()
      }
    }
  }

  private val Cursor.displayName: String?
    get() {
      val columnIndex = getColumnIndex(OpenableColumns.DISPLAY_NAME)
      return if (columnIndex != -1) getString(columnIndex) else null
    }
}
