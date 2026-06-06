package com.github.kr328.clash.profile.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.engine.android.AndroidProfileRepository
import com.github.kr328.clash.engine.api.ProfileRepository
import com.github.kr328.clash.glue.remote.Broadcasts
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.profile.R
import com.github.kr328.clash.profile.ui.ProfileActivationAction.Activate
import com.github.kr328.clash.profile.ui.ProfileActivationAction.RequireSave
import com.github.kr328.clash.profile.ui.ProfileUpdateAllAction.Ignore
import com.github.kr328.clash.profile.ui.ProfileUpdateAllAction.QueryProfiles
import com.github.kr328.clash.profile.ui.ProfilesBroadcastAction
import com.github.kr328.clash.profile.ui.ProfilesBroadcastEventKind
import com.github.kr328.clash.profile.ui.ProfilesUiState
import com.github.kr328.clash.profile.ui.profileActivationAction
import com.github.kr328.clash.profile.ui.profileUpdateAllAction
import com.github.kr328.clash.profile.ui.profileUpdateAllTargets
import com.github.kr328.clash.profile.ui.profilesBroadcastAction
import com.github.kr328.clash.profile.ui.withAllUpdating
import com.github.kr328.clash.profile.ui.withCurrentTime
import com.github.kr328.clash.profile.ui.withProfiles
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class ProfilesViewModel(app: Application) :
  AndroidViewModel(app), DefaultLifecycleObserver {
  private val profileRepository: ProfileRepository = AndroidProfileRepository()
  private var broadcastEventsJob: Job? = null
  private var elapsedJob: Job? = null
  private var fetchJob: Job? = null

  val uiState: StateFlow<ProfilesUiState>
    field = MutableStateFlow(ProfilesUiState(currentTime = System.currentTimeMillis()))

  val eventState: StateFlow<EventState>
    field = MutableStateFlow<EventState>(EventState.Idle)

  override fun onStart(owner: LifecycleOwner) {
    broadcastEventsJob?.cancel()
    broadcastEventsJob = viewModelScope.launch {
      Remote.broadcasts.event.collect { event ->
        when (val action = event.toProfilesBroadcastAction()) {
          ProfilesBroadcastAction.FetchProfiles -> fetch()
          ProfilesBroadcastAction.Ignore -> Unit
          is ProfilesBroadcastAction.ShowUpdateCompleted -> showProfileUpdateCompleted(action.uuid)
          is ProfilesBroadcastAction.ShowUpdateFailed ->
            showProfileUpdateFailed(action.uuid, action.reason)
        }
      }
    }

    startElapsedTicker()
    fetch()
  }

  override fun onStop(owner: LifecycleOwner) {
    broadcastEventsJob?.cancel()
    broadcastEventsJob = null
    elapsedJob?.cancel()
    elapsedJob = null
  }

  fun consumeEvent() {
    eventState.value = EventState.Idle
  }

  fun onOpenCreate() {
    eventState.value = EventState.OpenCreate
  }

  fun onActivate(profile: Profile) {
    viewModelScope.launch {
      when (profileActivationAction(profile)) {
        Activate -> profileRepository.setActive(profile)
        RequireSave -> {
          eventState.value =
            EventState.ShowEditableMessage(
              application.getString(R.string.active_unsaved_tips),
              profile.uuid,
            )
        }
      }
    }
  }

  fun onUpdateAll() {
    when (profileUpdateAllAction(uiState.value)) {
      Ignore -> return
      QueryProfiles ->
        viewModelScope.launch {
          uiState.update { it.withAllUpdating(true) }
          try {
            profileUpdateAllTargets(profileRepository.queryProfiles()).forEach { uuid ->
              profileRepository.update(uuid)
            }
          } finally {
            uiState.update { it.withAllUpdating(false) }
          }
        }
    }
  }

  fun onUpdate(profile: Profile) {
    viewModelScope.launch { profileRepository.update(profile.uuid) }
  }

  fun onEdit(profile: Profile) {
    eventState.value = EventState.OpenEdit(profile.uuid)
  }

  fun onDuplicate(profile: Profile) {
    viewModelScope.launch {
      val uuid = profileRepository.clone(profile.uuid)
      eventState.value = EventState.OpenEdit(uuid)
    }
  }

  fun onDelete(profile: Profile) {
    viewModelScope.launch { profileRepository.delete(profile.uuid) }
  }

  private fun fetch() {
    fetchJob?.cancel()
    fetchJob = viewModelScope.launch {
      val profiles = profileRepository.queryProfiles()

      uiState.update { it.withProfiles(profiles) }
    }
  }

  private fun startElapsedTicker() {
    if (elapsedJob?.isActive == true) return

    elapsedJob = viewModelScope.launch {
      while (isActive) {
        delay(1.minutes)
        uiState.update { it.withCurrentTime(System.currentTimeMillis()) }
      }
    }
  }

  private suspend fun showProfileUpdateCompleted(uuid: Uuid) {
    val name = profileRepository.queryByUuid(uuid)?.name
    eventState.value =
      EventState.ShowMessage(application.getString(R.string.toast_profile_updated_complete, name))
  }

  private suspend fun showProfileUpdateFailed(uuid: Uuid, reason: String?) {
    val name = profileRepository.queryByUuid(uuid)?.name
    val displayReason =
      reason?.takeUnless { it.isBlank() } ?: application.getString(CommonR.string.unknown)
    eventState.value =
      EventState.ShowEditableMessage(
        application.getString(R.string.toast_profile_updated_failed, name, displayReason),
        uuid,
      )
  }

  sealed interface EventState {
    data object Idle : EventState

    data object OpenCreate : EventState

    data class OpenEdit(val uuid: Uuid) : EventState

    data class ShowMessage(val message: String) : EventState

    data class ShowEditableMessage(val message: String, val uuid: Uuid) : EventState
  }

  private fun Broadcasts.Event.toProfilesBroadcastAction(): ProfilesBroadcastAction {
    return when (this) {
      Broadcasts.Event.ServiceRecreated ->
        profilesBroadcastAction(ProfilesBroadcastEventKind.ServiceRecreated)
      Broadcasts.Event.Started -> profilesBroadcastAction(ProfilesBroadcastEventKind.Started)
      is Broadcasts.Event.Stopped -> profilesBroadcastAction(ProfilesBroadcastEventKind.Stopped)
      Broadcasts.Event.ProfileChanged ->
        profilesBroadcastAction(ProfilesBroadcastEventKind.ProfileChanged)
      is Broadcasts.Event.ProfileUpdateCompleted ->
        profilesBroadcastAction(ProfilesBroadcastEventKind.ProfileUpdateCompleted, uuid = uuid)
      is Broadcasts.Event.ProfileUpdateFailed ->
        profilesBroadcastAction(
          ProfilesBroadcastEventKind.ProfileUpdateFailed,
          uuid = uuid,
          reason = reason,
        )
      Broadcasts.Event.ProfileLoaded ->
        profilesBroadcastAction(ProfilesBroadcastEventKind.ProfileLoaded)
    }
  }
}
