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
import com.github.kr328.clash.profile.ui.PropertiesUiState
import com.github.kr328.clash.profile.ui.withFetchConfigurationProgress
import com.github.kr328.clash.profile.ui.withFetchProvidersProgress
import com.github.kr328.clash.profile.ui.withLoadedProfile
import com.github.kr328.clash.profile.ui.withProcessingFinished
import com.github.kr328.clash.profile.ui.withProcessingStarted
import com.github.kr328.clash.profile.ui.withProfileInterval
import com.github.kr328.clash.profile.ui.withProfileName
import com.github.kr328.clash.profile.ui.withProfileSource
import com.github.kr328.clash.profile.ui.withProgress
import com.github.kr328.clash.profile.ui.withSavedProfile
import com.github.kr328.clash.profile.ui.withVerifyingProgress
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
    if (!canceled && uiState.value.hasUnsavedChanges) {
      val profile = uiState.value.profile ?: return
      viewModelScope.launch {
        runCatching {
            profileRepository.patch(profile.uuid, profile.name, profile.source, profile.interval)
          }
          .onFailure { e -> Log.e("Auto save profile failed: ${e.message}", e) }
          .onSuccess { uiState.update { state -> state.withSavedProfile(profile) } }
      }
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
    val profile = uiState.value.profile ?: return

    if (profile.name.isBlank()) {
      eventState.value = EventState.ShowMessage(application.getString(R.string.empty_name))
      return
    }

    if (profile.type != File && profile.source.isBlank()) {
      eventState.value = EventState.ShowMessage(application.getString(R.string.invalid_url))
      return
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
      val newProgress =
        when (status.action) {
          FetchConfiguration -> {
            current.progress.withFetchConfigurationProgress(
              text =
                application.getString(
                  R.string.format_fetching_configuration,
                  status.args.getOrNull(0).orEmpty(),
                )
            )
          }
          FetchProviders -> {
            current.progress.withFetchProvidersProgress(
              text =
                application.getString(
                  R.string.format_fetching_provider,
                  status.args.getOrNull(0).orEmpty(),
                ),
              max = status.max,
              progress = status.progress,
            )
          }
          Verifying -> {
            current.progress.withVerifyingProgress(
              text = application.getString(R.string.verifying),
              max = status.max,
              progress = status.progress,
            )
          }
        }
      current.withProgress(newProgress)
    }
  }

  sealed interface EventState {
    data object Idle : EventState

    data class Finish(val success: Boolean) : EventState

    data class BrowseFiles(val uuid: Uuid) : EventState

    data class ShowMessage(val message: String) : EventState
  }
}
