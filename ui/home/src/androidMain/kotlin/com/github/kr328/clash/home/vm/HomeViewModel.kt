package com.github.kr328.clash.home.vm

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.util.trafficTotal
import com.github.kr328.clash.engine.android.AndroidEngineController
import com.github.kr328.clash.engine.android.AndroidProfileRepository
import com.github.kr328.clash.engine.android.VpnPermissionRequiredException
import com.github.kr328.clash.engine.api.EngineController
import com.github.kr328.clash.engine.api.ProfileRepository
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.home.ui.HomeStartAction.ShowNoProfileMessage
import com.github.kr328.clash.home.ui.HomeStartAction.StartEngine
import com.github.kr328.clash.home.ui.HomeUiState
import com.github.kr328.clash.home.ui.homeStartAction
import com.github.kr328.clash.home.ui.withFetchedHomeState
import com.github.kr328.clash.home.ui.withForwardedTraffic
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class HomeViewModel(app: Application) : AndroidViewModel(app), DefaultLifecycleObserver {
  private val engineController: EngineController = AndroidEngineController(app)
  private val profileRepository: ProfileRepository = AndroidProfileRepository()
  private var broadcastEventsJob: Job? = null
  private var trafficPollingJob: Job? = null
  private var fetchJob: Job? = null

  val clashRunning: StateFlow<Boolean> = Remote.broadcasts.clashRunningFlow

  val uiState: StateFlow<HomeUiState>
    field = MutableStateFlow(HomeUiState())

  val eventState: StateFlow<EventState>
    field = MutableStateFlow<EventState>(EventState.Idle)

  override fun onStart(owner: LifecycleOwner) {
    broadcastEventsJob?.cancel()
    broadcastEventsJob = viewModelScope.launch {
      Remote.broadcasts.event.collect { event ->
        when (event) {
          ServiceRecreated,
          Started,
          ProfileChanged,
          ProfileLoaded -> fetch()
          is Stopped -> {
            event.cause?.let { message -> eventState.update { EventState.ShowMessage(message) } }
            fetch()
          }
          is ProfileUpdateCompleted,
          is ProfileUpdateFailed -> Unit
        }
      }
    }
    startTrafficPolling()
    fetch()
  }

  override fun onStop(owner: LifecycleOwner) {
    broadcastEventsJob?.cancel()
    broadcastEventsJob = null
    trafficPollingJob?.cancel()
    trafficPollingJob = null
  }

  fun toggleStatus() {
    if (clashRunning.value) {
      viewModelScope.launch { engineController.stop() }
    } else {
      startClash()
    }
  }

  fun onVpnPermissionGranted() {
    viewModelScope.launch { startEngine() }
  }

  fun consumeEvent() {
    eventState.value = EventState.Idle
  }

  private fun fetch() {
    fetchJob?.cancel()
    fetchJob = viewModelScope.launch {
      val state = engineController.queryState()
      val providers = engineController.queryProviders()
      val mode =
        when (state.mode) {
          Direct -> application.getString(CommonR.string.direct_mode)
          Global -> application.getString(CommonR.string.global_mode)
          Rule -> application.getString(CommonR.string.rule_mode)
        }
      val profileName = profileRepository.queryActive()?.name

      uiState.update {
        it.withFetchedHomeState(
          clashRunning = clashRunning.value,
          mode = mode,
          hasProviders = providers.isNotEmpty(),
          profileName = profileName,
        )
      }
    }
  }

  private fun startTrafficPolling() {
    if (trafficPollingJob?.isActive == true) return
    trafficPollingJob = viewModelScope.launch {
      while (isActive) {
        delay(1.seconds)
        if (clashRunning.value) {
          val total = engineController.queryTraffic()
          uiState.update { it.withForwardedTraffic(total.trafficTotal()) }
        }
      }
    }
  }

  private fun startClash() {
    viewModelScope.launch {
      when (homeStartAction(profileRepository.queryActive())) {
        StartEngine -> startEngine()
        ShowNoProfileMessage -> eventState.value = EventState.ShowNoProfileMessage
      }
    }
  }

  private suspend fun startEngine() {
    try {
      engineController.start()
    } catch (e: VpnPermissionRequiredException) {
      eventState.value = EventState.RequestVpnPermission(e.prepareIntent)
    } catch (e: Exception) {
      Log.e("Start clash service failed: ${e.message}", e)
      eventState.value =
        EventState.ShowMessage(application.getString(CommonR.string.unable_to_start_vpn))
    }
  }

  sealed interface EventState {
    data object Idle : EventState

    data class RequestVpnPermission(val intent: Intent) : EventState

    data object ShowNoProfileMessage : EventState

    data class ShowMessage(val message: String) : EventState
  }
}
