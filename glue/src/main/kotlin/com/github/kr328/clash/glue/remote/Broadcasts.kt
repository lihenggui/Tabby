package com.github.kr328.clash.glue.remote

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.github.kr328.clash.common.compat.registerReceiverCompat
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.util.getSerializableCompat
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class Broadcasts(private val context: Application) {
  sealed interface Event {
    data object ServiceRecreated : Event

    data object Loading : Event

    data object Started : Event

    data class Stopped(val cause: String?) : Event

    data object ProfileChanged : Event

    data class ProfileUpdateCompleted(val uuid: Uuid?) : Event

    data class ProfileUpdateFailed(val uuid: Uuid?, val reason: String?) : Event

    data object ProfileLoaded : Event
  }

  val clashRunningFlow: StateFlow<Boolean>
    field = MutableStateFlow(false)

  val clashServiceStateFlow: StateFlow<ClashServiceState>
    field = MutableStateFlow<ClashServiceState>(ClashServiceState.Stopped)

  val event: SharedFlow<Event>
    field = MutableSharedFlow(extraBufferCapacity = 64)

  var clashServiceState: ClashServiceState
    get() = clashServiceStateFlow.value
    private set(value) {
      clashServiceStateFlow.value = value
      clashRunningFlow.value = value != ClashServiceState.Stopped
    }

  val clashRunning: Boolean
    get() = clashServiceState != ClashServiceState.Stopped

  private var registered = false
  private val broadcastReceiver =
    object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.`package` != context?.packageName) return

        when (intent?.action) {
          Intents.ACTION_SERVICE_RECREATED -> {
            clashServiceState = ClashServiceState.Stopped
            event.tryEmit(Event.ServiceRecreated)
          }
          Intents.ACTION_CLASH_LOADING -> {
            clashServiceState = ClashServiceState.Loading
            event.tryEmit(Event.Loading)
          }
          Intents.ACTION_CLASH_STARTED -> {
            clashServiceState = ClashServiceState.Running
            event.tryEmit(Event.Started)
          }
          Intents.ACTION_CLASH_STOPPED -> {
            clashServiceState = ClashServiceState.Stopped
            event.tryEmit(Event.Stopped(intent.getStringExtra(Intents.EXTRA_STOP_REASON)))
          }
          Intents.ACTION_PROFILE_CHANGED -> event.tryEmit(Event.ProfileChanged)
          Intents.ACTION_PROFILE_UPDATE_COMPLETED ->
            event.tryEmit(
              Event.ProfileUpdateCompleted(intent.getSerializableCompat(Intents.EXTRA_UUID))
            )
          Intents.ACTION_PROFILE_UPDATE_FAILED ->
            event.tryEmit(
              Event.ProfileUpdateFailed(
                intent.getSerializableCompat(Intents.EXTRA_UUID),
                intent.getStringExtra(Intents.EXTRA_FAIL_REASON),
              )
            )
          Intents.ACTION_PROFILE_LOADED -> {
            clashServiceState = ClashServiceState.Running
            event.tryEmit(Event.ProfileLoaded)
          }
        }
      }
    }

  fun register() {
    if (registered) return

    try {
      context.registerReceiverCompat(
        broadcastReceiver,
        IntentFilter().apply {
          addAction(Intents.ACTION_SERVICE_RECREATED)
          addAction(Intents.ACTION_CLASH_LOADING)
          addAction(Intents.ACTION_CLASH_STARTED)
          addAction(Intents.ACTION_CLASH_STOPPED)
          addAction(Intents.ACTION_PROFILE_CHANGED)
          addAction(Intents.ACTION_PROFILE_UPDATE_COMPLETED)
          addAction(Intents.ACTION_PROFILE_UPDATE_FAILED)
          addAction(Intents.ACTION_PROFILE_LOADED)
        },
      )
      registered = true

      clashServiceState = StatusClient(context).serviceState()
    } catch (e: Exception) {
      Log.w("Register global receiver: $e", e)
    }
  }
}
