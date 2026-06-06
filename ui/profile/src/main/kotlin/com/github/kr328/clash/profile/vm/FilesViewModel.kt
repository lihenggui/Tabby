package com.github.kr328.clash.profile.vm

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.engine.android.AndroidProfileRepository
import com.github.kr328.clash.engine.api.ProfileRepository
import com.github.kr328.clash.glue.model.ConfigFile
import com.github.kr328.clash.glue.remote.FilesClient
import com.github.kr328.clash.glue.util.fileName
import com.github.kr328.clash.profile.ui.ProfileFilesLocation
import com.github.kr328.clash.profile.ui.selectVisibleProfileFiles
import kotlin.uuid.Uuid
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class FilesViewModel(app: Application) : AndroidViewModel(app), DefaultLifecycleObserver {
  private val profileRepository: ProfileRepository = AndroidProfileRepository()
  private val client = FilesClient(app)
  private var location = ProfileFilesLocation()
  private var fetchJob: Job? = null

  val uiState: StateFlow<UiState>
    field = MutableStateFlow(UiState())

  val eventState: StateFlow<EventState>
    field = MutableStateFlow<EventState>(EventState.Idle)

  fun init(uuid: Uuid) {
    if (location.initialized) return
    location = location.initialize(uuid.toString())

    viewModelScope.launch {
      val profile = profileRepository.queryByUuid(uuid)
      if (profile == null) {
        eventState.value = EventState.Finish
        return@launch
      }
      uiState.update { it.copy(configurationEditable = profile.type == Url) }
      fetch()
    }
  }

  fun consumeEvent() {
    eventState.value = EventState.Idle
  }

  override fun onStart(owner: LifecycleOwner) {
    if (location.initialized) {
      fetch()
    }
  }

  fun onBack() {
    val updatedLocation = location.leaveDirectory()
    if (updatedLocation == null) {
      eventState.value = EventState.Finish
    } else {
      location = updatedLocation
      fetch()
    }
  }

  fun onOpen(configFile: ConfigFile) {
    if (configFile.isDirectory) {
      location = location.enterDirectory(configFile.id)
      fetch()
    } else {
      val uri = client.buildDocumentUri(configFile.id)
      eventState.value = EventState.OpenFile(uri)
    }
  }

  fun onDelete(configFile: ConfigFile) {
    viewModelScope.launch {
      try {
        client.deleteDocument(configFile.id)
      } catch (e: Exception) {
        Log.e("Delete file failed: ${e.message}", e)
        eventState.value = EventState.ShowMessage(e.message ?: "Unknown error")
      }
      fetch()
    }
  }

  fun onRename(configFile: ConfigFile, newName: String) {
    viewModelScope.launch {
      try {
        client.renameDocument(configFile.id, newName)
      } catch (e: Exception) {
        Log.e("Rename file failed: ${e.message}", e)
        eventState.value = EventState.ShowMessage(e.message ?: "Unknown error")
      }
      fetch()
    }
  }

  fun onRequestImport(configFile: ConfigFile?) {
    eventState.value = EventState.RequestImport(configFile)
  }

  fun onImportResult(uri: Uri?, targetConfigFile: ConfigFile?) {
    if (uri == null) return
    val parentId = location.currentDocumentId
    viewModelScope.launch {
      try {
        if (targetConfigFile == null) {
          client.importDocument(parentId, uri, uri.fileName ?: "File")
        } else {
          client.copyDocument(targetConfigFile.id, uri)
        }
      } catch (e: Exception) {
        Log.e("Import file failed: ${e.message}", e)
        eventState.value = EventState.ShowMessage(e.message ?: "Unknown error")
      }
      fetch()
    }
  }

  fun onRequestExport(configFile: ConfigFile) {
    eventState.value = EventState.RequestExport(configFile)
  }

  fun onExportResult(uri: Uri?, sourceConfigFile: ConfigFile?) {
    if (uri == null || sourceConfigFile == null) return
    viewModelScope.launch {
      try {
        client.copyDocument(uri, sourceConfigFile.id)
      } catch (e: Exception) {
        Log.e("Export file failed: ${e.message}", e)
        eventState.value = EventState.ShowMessage(e.message ?: "Unknown error")
      }
      fetch()
    }
  }

  private fun fetch() {
    fetchJob?.cancel()
    if (!location.initialized) return
    val documentId = location.currentDocumentId
    val inBaseDir = location.currentInBaseDir
    fetchJob = viewModelScope.launch {
      try {
        val files =
          selectVisibleProfileFiles(
            files = client.list(documentId),
            inBaseDirectory = inBaseDir,
            id = ConfigFile::id,
            size = ConfigFile::size,
          )

        uiState.update { it.copy(configFiles = files, currentInBaseDir = inBaseDir) }
      } catch (e: Exception) {
        Log.e("List files failed: ${e.message}", e)
        eventState.value = EventState.ShowMessage(e.message ?: "Unknown error")
      }
    }
  }

  data class UiState(
    val configFiles: List<ConfigFile> = emptyList(),
    val currentInBaseDir: Boolean = true,
    val configurationEditable: Boolean = false,
  )

  sealed interface EventState {
    data object Idle : EventState

    data object Finish : EventState

    data class OpenFile(val uri: Uri) : EventState

    data class RequestImport(val targetConfigFile: ConfigFile?) : EventState

    data class RequestExport(val sourceConfigFile: ConfigFile) : EventState

    data class ShowMessage(val message: String) : EventState
  }
}
