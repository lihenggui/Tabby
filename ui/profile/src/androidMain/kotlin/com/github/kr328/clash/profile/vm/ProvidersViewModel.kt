package com.github.kr328.clash.profile.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.engine.android.AndroidEngineController
import com.github.kr328.clash.engine.api.EngineController
import com.github.kr328.clash.glue.remote.Broadcasts
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.profile.R
import com.github.kr328.clash.profile.ui.ProvidersBroadcastAction
import com.github.kr328.clash.profile.ui.ProvidersBroadcastEventKind
import com.github.kr328.clash.profile.ui.ProvidersEventState
import com.github.kr328.clash.profile.ui.ProvidersUiState
import com.github.kr328.clash.profile.ui.ProvidersUpdateAllAction
import com.github.kr328.clash.profile.ui.providerUpdateFailureEventState
import com.github.kr328.clash.profile.ui.providersBroadcastAction
import com.github.kr328.clash.profile.ui.providersConsumedEventState
import com.github.kr328.clash.profile.ui.providersUpdateAllAction
import com.github.kr328.clash.profile.ui.withCurrentTime
import com.github.kr328.clash.profile.ui.withFetchedProviders
import com.github.kr328.clash.profile.ui.withProviderUpdateFailed
import com.github.kr328.clash.profile.ui.withProviderUpdateStarted
import com.github.kr328.clash.profile.ui.withProviderUpdateSucceeded
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class ProvidersViewModel(app: Application) :
  AndroidViewModel(app), DefaultLifecycleObserver {
  private val engineController: EngineController = AndroidEngineController(app)
  private var broadcastEventsJob: Job? = null
  private var elapsedJob: Job? = null
  private var fetchJob: Job? = null

  val uiState: StateFlow<ProvidersUiState>
    field = MutableStateFlow(ProvidersUiState(currentTime = System.currentTimeMillis()))

  val eventState: StateFlow<ProvidersEventState>
    field = MutableStateFlow<ProvidersEventState>(ProvidersEventState.Idle)

  override fun onStart(owner: LifecycleOwner) {
    broadcastEventsJob?.cancel()
    broadcastEventsJob = viewModelScope.launch {
      Remote.broadcasts.event.collect { event ->
        when (event.toProvidersBroadcastAction()) {
          ProvidersBroadcastAction.FetchProviders -> fetch()
          ProvidersBroadcastAction.Ignore -> Unit
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
    eventState.value = providersConsumedEventState()
  }

  fun onUpdateAll() {
    when (val action = providersUpdateAllAction(uiState.value)) {
      is ProvidersUpdateAllAction.UpdateProviders -> action.providers.forEach(::onUpdate)
      ProvidersUpdateAllAction.Ignore -> Unit
    }
  }

  fun onUpdate(provider: Provider) {
    uiState.update { current -> current.withProviderUpdateStarted(provider) }

    viewModelScope.launch {
      try {
        engineController.updateProvider(provider.type, provider.name)
        uiState.update { current ->
          current.withProviderUpdateSucceeded(provider, updatedAt = System.currentTimeMillis())
        }
      } catch (e: Exception) {
        Log.e("Update provider ${provider.name} failed: ${e.message}", e)
        uiState.update { current -> current.withProviderUpdateFailed(provider) }
        val errorMessage = e.localizedMessage ?: e.message ?: e.toString()
        eventState.value =
          providerUpdateFailureEventState(
            providerName = provider.name,
            errorMessage = errorMessage,
          ) { name, message ->
            application.getString(R.string.format_update_provider_failure, name, message)
          }
      }
    }
  }

  private fun fetch() {
    fetchJob?.cancel()
    fetchJob = viewModelScope.launch {
      val providers = engineController.queryProviders()
      uiState.update { current -> current.withFetchedProviders(providers) }
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

  private fun Broadcasts.Event.toProvidersBroadcastAction(): ProvidersBroadcastAction {
    return when (this) {
      Broadcasts.Event.ServiceRecreated ->
        providersBroadcastAction(ProvidersBroadcastEventKind.ServiceRecreated)
      Broadcasts.Event.Started -> providersBroadcastAction(ProvidersBroadcastEventKind.Started)
      is Broadcasts.Event.Stopped -> providersBroadcastAction(ProvidersBroadcastEventKind.Stopped)
      Broadcasts.Event.ProfileChanged ->
        providersBroadcastAction(ProvidersBroadcastEventKind.ProfileChanged)
      is Broadcasts.Event.ProfileUpdateCompleted ->
        providersBroadcastAction(ProvidersBroadcastEventKind.ProfileUpdateCompleted)
      is Broadcasts.Event.ProfileUpdateFailed ->
        providersBroadcastAction(ProvidersBroadcastEventKind.ProfileUpdateFailed)
      Broadcasts.Event.ProfileLoaded ->
        providersBroadcastAction(ProvidersBroadcastEventKind.ProfileLoaded)
    }
  }
}
