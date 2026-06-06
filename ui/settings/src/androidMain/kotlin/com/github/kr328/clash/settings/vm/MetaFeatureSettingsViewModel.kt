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
import com.github.kr328.clash.settings.ui.GeoFileImportType
import com.github.kr328.clash.settings.ui.MetaFeatureSettingsActions
import com.github.kr328.clash.settings.ui.OverridePersistAction
import com.github.kr328.clash.settings.ui.SniffProtocol
import com.github.kr328.clash.settings.ui.geoFileImportAction
import com.github.kr328.clash.settings.ui.geoFileImportFailedResult
import com.github.kr328.clash.settings.ui.geoFileImportInitialResult
import com.github.kr328.clash.settings.ui.geoFileImportResult
import com.github.kr328.clash.settings.ui.geoFileImportStartedResult
import com.github.kr328.clash.settings.ui.metaFeatureSettingsInitialConfiguration
import com.github.kr328.clash.settings.ui.overridePersistAction
import com.github.kr328.clash.settings.ui.updateMetaFindProcessMode
import com.github.kr328.clash.settings.ui.updateMetaGeodataMode
import com.github.kr328.clash.settings.ui.updateMetaSnifferEnable
import com.github.kr328.clash.settings.ui.updateMetaSnifferForceDnsMapping
import com.github.kr328.clash.settings.ui.updateMetaSnifferForceDomain
import com.github.kr328.clash.settings.ui.updateMetaSnifferOverrideDestination
import com.github.kr328.clash.settings.ui.updateMetaSnifferParsePureIp
import com.github.kr328.clash.settings.ui.updateMetaSnifferSkipDomain
import com.github.kr328.clash.settings.ui.updateMetaSnifferSkipDstAddress
import com.github.kr328.clash.settings.ui.updateMetaSnifferSkipSrcAddress
import com.github.kr328.clash.settings.ui.updateMetaTcpConcurrent
import com.github.kr328.clash.settings.ui.updateMetaUnifiedDelay
import com.github.kr328.clash.settings.ui.updateSniffProtocolOverrideDestination
import com.github.kr328.clash.settings.ui.updateSniffProtocolPorts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class MetaFeatureSettingsViewModel(app: Application) :
  AndroidViewModel(app), MetaFeatureSettingsActions, DefaultLifecycleObserver {
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

  fun importGeoFile(uri: Uri?, importType: GeoFileImportType) {
    viewModelScope.launch(Dispatchers.IO) {
      importResult.value = geoFileImportStartedResult()
      try {
        val resolver = appContext.contentResolver
        val cursor: Cursor =
          uri?.let { resolver.query(it, null, null, null, null, null) }
            ?: run {
              importResult.value = geoFileImportFailedResult()
              return@launch
            }

        importResult.value = cursor.use {
          if (!it.moveToFirst()) return@use geoFileImportFailedResult()

          val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          val displayName = if (columnIndex != -1) it.getString(columnIndex).orEmpty() else ""

          when (
            val action = geoFileImportAction(displayName = displayName, importType = importType)
          ) {
            is GeoFileImportAction.UnsupportedFormat -> return@use geoFileImportResult(action)
            is GeoFileImportAction.Copy -> {
              val outputFile = appContext.clashDir.resolve(action.outputFileName)
              outputFile.parentFile?.mkdirs()
              val inputStream =
                resolver.openInputStream(uri)
                  ?: return@use geoFileImportResult(action, copySucceeded = false)
              inputStream.use { ins -> outputFile.outputStream().use { outs -> ins.copyTo(outs) } }
              return@use geoFileImportResult(action, copySucceeded = true)
            }
          }
        }
      } catch (e: Exception) {
        Log.e("Import geo database failed: ${e.message}", e)
        importResult.value = geoFileImportFailedResult()
      }
    }
  }

  override fun updateUnifiedDelay(value: Boolean?) = configuration.update {
    updateMetaUnifiedDelay(it, value)
  }

  override fun updateGeodataMode(value: Boolean?) = configuration.update {
    updateMetaGeodataMode(it, value)
  }

  override fun updateTcpConcurrent(value: Boolean?) = configuration.update {
    updateMetaTcpConcurrent(it, value)
  }

  override fun updateFindProcessMode(value: ConfigurationOverride.FindProcessMode?) =
    configuration.update {
      updateMetaFindProcessMode(it, value)
    }

  override fun updateSnifferEnable(value: Boolean?) = configuration.update {
    updateMetaSnifferEnable(it, value)
  }

  override fun updateSniffHttpPorts(value: List<String>?) = configuration.update {
    updateSniffProtocolPorts(it, SniffProtocol.Http, value)
  }

  override fun updateSniffHttpOverrideDestination(value: Boolean?) = configuration.update {
    updateSniffProtocolOverrideDestination(it, SniffProtocol.Http, value)
  }

  override fun updateSniffTlsPorts(value: List<String>?) = configuration.update {
    updateSniffProtocolPorts(it, SniffProtocol.Tls, value)
  }

  override fun updateSniffTlsOverrideDestination(value: Boolean?) = configuration.update {
    updateSniffProtocolOverrideDestination(it, SniffProtocol.Tls, value)
  }

  override fun updateSniffQuicPorts(value: List<String>?) = configuration.update {
    updateSniffProtocolPorts(it, SniffProtocol.Quic, value)
  }

  override fun updateSniffQuicOverrideDestination(value: Boolean?) = configuration.update {
    updateSniffProtocolOverrideDestination(it, SniffProtocol.Quic, value)
  }

  override fun updateForceDnsMapping(value: Boolean?) = configuration.update {
    updateMetaSnifferForceDnsMapping(it, value)
  }

  override fun updateParsePureIp(value: Boolean?) = configuration.update {
    updateMetaSnifferParsePureIp(it, value)
  }

  override fun updateOverrideDestination(value: Boolean?) = configuration.update {
    updateMetaSnifferOverrideDestination(it, value)
  }

  override fun updateForceDomain(value: List<String>?) = configuration.update {
    updateMetaSnifferForceDomain(it, value)
  }

  override fun updateSkipDomain(value: List<String>?) = configuration.update {
    updateMetaSnifferSkipDomain(it, value)
  }

  override fun updateSkipSrcAddress(value: List<String>?) = configuration.update {
    updateMetaSnifferSkipSrcAddress(it, value)
  }

  override fun updateSkipDstAddress(value: List<String>?) = configuration.update {
    updateMetaSnifferSkipDstAddress(it, value)
  }
}
