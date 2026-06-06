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
import com.github.kr328.clash.glue.remote.Broadcasts
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.home.ui.HomeBroadcastEventKind
import com.github.kr328.clash.home.ui.HomeEventState
import com.github.kr328.clash.home.ui.HomeToggleAction.StartClash
import com.github.kr328.clash.home.ui.HomeToggleAction.StopClash
import com.github.kr328.clash.home.ui.HomeTrafficPollAction.Ignore
import com.github.kr328.clash.home.ui.HomeTrafficPollAction.QueryTraffic
import com.github.kr328.clash.home.ui.HomeUiState
import com.github.kr328.clash.home.ui.homeBroadcastAction
import com.github.kr328.clash.home.ui.homeBroadcastEventState
import com.github.kr328.clash.home.ui.homeStartAction
import com.github.kr328.clash.home.ui.homeStartEventState
import com.github.kr328.clash.home.ui.homeStartFailureEventState
import com.github.kr328.clash.home.ui.homeToggleAction
import com.github.kr328.clash.home.ui.homeTrafficPollAction
import com.github.kr328.clash.home.ui.homeVpnPermissionEventState
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

  val eventState: StateFlow<HomeEventState<Intent>>
    field = MutableStateFlow<HomeEventState<Intent>>(HomeEventState.Idle)

  override fun onStart(owner: LifecycleOwner) {
    broadcastEventsJob?.cancel()
    broadcastEventsJob = viewModelScope.launch {
      Remote.broadcasts.event.collect { event ->
        val action = event.toHomeBroadcastAction()

        homeBroadcastEventState(action)?.let { eventState.value = it }
        if (action.shouldFetch) fetch()
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
    when (homeToggleAction(clashRunning.value)) {
      StartClash -> startClash()
      StopClash -> viewModelScope.launch { engineController.stop() }
    }
  }

  fun onVpnPermissionGranted() {
    viewModelScope.launch { startEngine() }
  }

  fun consumeEvent() {
    eventState.value = HomeEventState.Idle
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
        when (homeTrafficPollAction(clashRunning.value)) {
          QueryTraffic -> {
            val total = engineController.queryTraffic()
            uiState.update { it.withForwardedTraffic(total.trafficTotal()) }
          }
          Ignore -> Unit
        }
      }
    }
  }

  private fun startClash() {
    viewModelScope.launch {
      val action = homeStartAction(profileRepository.queryActive())
      val event = homeStartEventState(action)

      if (event != null) eventState.value = event else startEngine()
    }
  }

  private suspend fun startEngine() {
    try {
      engineController.start()
    } catch (e: VpnPermissionRequiredException) {
      eventState.value = homeVpnPermissionEventState(e.prepareIntent)
    } catch (e: Exception) {
      Log.e("Start clash service failed: ${e.message}", e)
      eventState.value =
        homeStartFailureEventState(application.getString(CommonR.string.unable_to_start_vpn))
    }
  }

  private fun Broadcasts.Event.toHomeBroadcastAction() =
    when (this) {
      Broadcasts.Event.ServiceRecreated ->
        homeBroadcastAction(HomeBroadcastEventKind.ServiceRecreated)
      Broadcasts.Event.Started -> homeBroadcastAction(HomeBroadcastEventKind.Started)
      is Broadcasts.Event.Stopped ->
        homeBroadcastAction(HomeBroadcastEventKind.Stopped, stoppedMessage = cause)
      Broadcasts.Event.ProfileChanged -> homeBroadcastAction(HomeBroadcastEventKind.ProfileChanged)
      is Broadcasts.Event.ProfileUpdateCompleted ->
        homeBroadcastAction(HomeBroadcastEventKind.ProfileUpdateCompleted)
      is Broadcasts.Event.ProfileUpdateFailed ->
        homeBroadcastAction(HomeBroadcastEventKind.ProfileUpdateFailed)
      Broadcasts.Event.ProfileLoaded -> homeBroadcastAction(HomeBroadcastEventKind.ProfileLoaded)
    }
}
