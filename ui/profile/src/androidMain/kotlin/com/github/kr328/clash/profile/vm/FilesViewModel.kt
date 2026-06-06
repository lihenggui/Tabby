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
import com.github.kr328.clash.profile.ui.ProfileFileExportAction
import com.github.kr328.clash.profile.ui.ProfileFileImportAction
import com.github.kr328.clash.profile.ui.ProfileFileOpenAction
import com.github.kr328.clash.profile.ui.ProfileFilesBackAction
import com.github.kr328.clash.profile.ui.ProfileFilesFetchAction
import com.github.kr328.clash.profile.ui.ProfileFilesLocation
import com.github.kr328.clash.profile.ui.ProfileFilesUiState
import com.github.kr328.clash.profile.ui.isProfileConfigurationEditable
import com.github.kr328.clash.profile.ui.profileFileExportAction
import com.github.kr328.clash.profile.ui.profileFileImportAction
import com.github.kr328.clash.profile.ui.profileFileOpenAction
import com.github.kr328.clash.profile.ui.profileFilesBackAction
import com.github.kr328.clash.profile.ui.profileFilesFetchAction
import com.github.kr328.clash.profile.ui.selectVisibleProfileFiles
import com.github.kr328.clash.profile.ui.withConfigFiles
import com.github.kr328.clash.profile.ui.withConfigurationEditable
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

  val uiState: StateFlow<ProfileFilesUiState<ConfigFile>>
    field = MutableStateFlow(ProfileFilesUiState())

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
      uiState.update { it.withConfigurationEditable(isProfileConfigurationEditable(profile)) }
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
    when (val action = profileFilesBackAction(location)) {
      is ProfileFilesBackAction.LeaveDirectory -> {
        location = action.location
        fetch()
      }
      ProfileFilesBackAction.Finish -> eventState.value = EventState.Finish
    }
  }

  fun onOpen(configFile: ConfigFile) {
    when (val action = profileFileOpenAction(configFile.id, configFile.isDirectory)) {
      is ProfileFileOpenAction.EnterDirectory -> {
        location = location.enterDirectory(action.documentId)
        fetch()
      }
      is ProfileFileOpenAction.OpenFile -> {
        val uri = client.buildDocumentUri(action.documentId)
        eventState.value = EventState.OpenFile(uri)
      }
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
    val sourceUri = uri
    val action =
      profileFileImportAction(
        sourceSelected = sourceUri != null,
        sourceFileName = sourceUri?.fileName,
        targetDocumentId = targetConfigFile?.id,
        parentDocumentId = location.currentDocumentId,
      )

    if (action == ProfileFileImportAction.Ignore) return
    val selectedUri = checkNotNull(sourceUri)

    viewModelScope.launch {
      try {
        when (action) {
          is ProfileFileImportAction.ImportNewFile ->
            client.importDocument(action.parentDocumentId, selectedUri, action.fileName)
          is ProfileFileImportAction.ReplaceFile ->
            client.copyDocument(action.targetDocumentId, selectedUri)
          ProfileFileImportAction.Ignore -> Unit
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
    val outputUri = uri
    val action =
      profileFileExportAction(
        outputSelected = outputUri != null,
        sourceDocumentId = sourceConfigFile?.id,
      )

    if (action == ProfileFileExportAction.Ignore) return
    val selectedOutputUri = checkNotNull(outputUri)

    viewModelScope.launch {
      try {
        when (action) {
          is ProfileFileExportAction.ExportFile ->
            client.copyDocument(selectedOutputUri, action.sourceDocumentId)
          ProfileFileExportAction.Ignore -> Unit
        }
      } catch (e: Exception) {
        Log.e("Export file failed: ${e.message}", e)
        eventState.value = EventState.ShowMessage(e.message ?: "Unknown error")
      }
      fetch()
    }
  }

  private fun fetch() {
    fetchJob?.cancel()
    val fetchAction =
      when (val action = profileFilesFetchAction(location)) {
        is ProfileFilesFetchAction.Fetch -> action
        ProfileFilesFetchAction.Ignore -> return
      }
    fetchJob = viewModelScope.launch {
      try {
        val files =
          selectVisibleProfileFiles(
            files = client.list(fetchAction.documentId),
            inBaseDirectory = fetchAction.inBaseDirectory,
            id = ConfigFile::id,
            size = ConfigFile::size,
          )

        uiState.update {
          it.withConfigFiles(
            configFiles = files,
            currentInBaseDir = fetchAction.inBaseDirectory,
          )
        }
      } catch (e: Exception) {
        Log.e("List files failed: ${e.message}", e)
        eventState.value = EventState.ShowMessage(e.message ?: "Unknown error")
      }
    }
  }

  sealed interface EventState {
    data object Idle : EventState

    data object Finish : EventState

    data class OpenFile(val uri: Uri) : EventState

    data class RequestImport(val targetConfigFile: ConfigFile?) : EventState

    data class RequestExport(val sourceConfigFile: ConfigFile) : EventState

    data class ShowMessage(val message: String) : EventState
  }
}
