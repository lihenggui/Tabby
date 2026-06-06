package com.github.kr328.clash.profile.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.model.FetchStatus
import com.github.kr328.clash.engine.android.AndroidProfileRepository
import com.github.kr328.clash.engine.api.ProfileRepository
import com.github.kr328.clash.profile.R
import com.github.kr328.clash.profile.ui.PropertiesAutoSaveAction
import com.github.kr328.clash.profile.ui.PropertiesCommitAction.Commit
import com.github.kr328.clash.profile.ui.PropertiesCommitAction.Ignore
import com.github.kr328.clash.profile.ui.PropertiesCommitAction.ShowEmptyName
import com.github.kr328.clash.profile.ui.PropertiesCommitAction.ShowEmptySource
import com.github.kr328.clash.profile.ui.PropertiesUiState
import com.github.kr328.clash.profile.ui.propertiesAutoSaveAction
import com.github.kr328.clash.profile.ui.propertiesCommitAction
import com.github.kr328.clash.profile.ui.withFetchStatusProgress
import com.github.kr328.clash.profile.ui.withLoadedProfile
import com.github.kr328.clash.profile.ui.withProcessingFinished
import com.github.kr328.clash.profile.ui.withProcessingStarted
import com.github.kr328.clash.profile.ui.withProfileInterval
import com.github.kr328.clash.profile.ui.withProfileName
import com.github.kr328.clash.profile.ui.withProfileSource
import com.github.kr328.clash.profile.ui.withSavedProfile
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class PropertiesViewModel(app: Application) :
  AndroidViewModel(app), DefaultLifecycleObserver {
  private val profileRepository: ProfileRepository = AndroidProfileRepository()
  private var rootUuid: Uuid? = null
  private var canceled = false

  val uiState: StateFlow<PropertiesUiState>
    field = MutableStateFlow(PropertiesUiState())

  val eventState: StateFlow<EventState>
    field = MutableStateFlow<EventState>(EventState.Idle)

  fun init(uuid: Uuid) {
    if (rootUuid != null) return
    rootUuid = uuid

    viewModelScope.launch {
      val profile = profileRepository.queryByUuid(uuid)
      if (profile == null) {
        eventState.value = EventState.Finish(false)
        return@launch
      }
      uiState.update { it.withLoadedProfile(profile) }
    }
  }

  fun consumeEvent() {
    eventState.value = EventState.Idle
  }

  override fun onStop(owner: LifecycleOwner) {
    when (val action = propertiesAutoSaveAction(canceled, uiState.value)) {
      is PropertiesAutoSaveAction.Save -> {
        val profile = action.profile

        viewModelScope.launch {
          runCatching {
              profileRepository.patch(profile.uuid, profile.name, profile.source, profile.interval)
            }
            .onFailure { e -> Log.e("Auto save profile failed: ${e.message}", e) }
            .onSuccess { uiState.update { state -> state.withSavedProfile(profile) } }
        }
      }
      PropertiesAutoSaveAction.Ignore -> Unit
    }
  }

  override fun onCleared() {
    rootUuid?.let { uuid ->
      Global.launch {
        try {
          profileRepository.release(uuid)
        } catch (e: Exception) {
          Log.e("Release profile failed: ${e.message}", e)
        }
      }
    }
  }

  fun onNameChanged(name: String) {
    uiState.update { current -> current.withProfileName(name) }
  }

  fun onUrlChanged(url: String) {
    uiState.update { current -> current.withProfileSource(url) }
  }

  fun onIntervalChanged(interval: Long) {
    uiState.update { current -> current.withProfileInterval(interval) }
  }

  fun onBrowseFiles() {
    val uuid = rootUuid ?: return
    eventState.value = EventState.BrowseFiles(uuid)
  }

  fun onRequestClose() {
    canceled = true
    eventState.value = EventState.Finish(false)
  }

  fun onCommit() {
    val profile =
      when (val action = propertiesCommitAction(uiState.value)) {
        Ignore -> return
        ShowEmptyName -> {
          eventState.value = EventState.ShowMessage(application.getString(R.string.empty_name))
          return
        }
        ShowEmptySource -> {
          eventState.value = EventState.ShowMessage(application.getString(R.string.invalid_url))
          return
        }
        is Commit -> action.profile
      }

    viewModelScope.launch {
      try {
        withProcessing { updateStatus ->
          profileRepository.patch(profile.uuid, profile.name, profile.source, profile.interval)
          coroutineScope {
            profileRepository.commit(profile.uuid) { status -> launch { updateStatus(status) } }
          }
        }
        canceled = true
        eventState.value = EventState.Finish(true)
      } catch (e: Exception) {
        Log.e("Commit profile failed: ${e.message}", e)
        eventState.value =
          EventState.ShowMessage(e.message ?: application.getString(CommonR.string.unknown))
      }
    }
  }

  private suspend fun withProcessing(executeTask: suspend (suspend (FetchStatus) -> Unit) -> Unit) {
    try {
      withContext(Dispatchers.Main) {
        uiState.update { it.withProcessingStarted(application.getString(R.string.initializing)) }
      }

      executeTask { status -> withContext(Dispatchers.Main) { applyProgressStatus(status) } }
    } finally {
      withContext(Dispatchers.Main) {
        uiState.update { it.withProcessingFinished() }
      }
    }
  }

  private fun applyProgressStatus(status: FetchStatus) {
    uiState.update { current ->
      current.withFetchStatusProgress(
        status = status,
        formatFetchConfiguration = { source ->
          application.getString(R.string.format_fetching_configuration, source)
        },
        formatFetchProvider = { provider ->
          application.getString(R.string.format_fetching_provider, provider)
        },
        verifyingText = application.getString(R.string.verifying),
      )
    }
  }

  sealed interface EventState {
    data object Idle : EventState

    data class Finish(val success: Boolean) : EventState

    data class BrowseFiles(val uuid: Uuid) : EventState

    data class ShowMessage(val message: String) : EventState
  }
}
