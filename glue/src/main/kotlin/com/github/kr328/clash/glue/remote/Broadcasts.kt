package com.github.kr328.clash.glue.remote

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.github.kr328.clash.common.compat.registerReceiverCompat
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.remote.TabbyRemoteBroadcastAction
import com.github.kr328.clash.common.remote.tabbyRemoteBroadcastActionFromPlatformAction
import com.github.kr328.clash.common.remote.tabbyRemoteBroadcastClashRunningState
import com.github.kr328.clash.common.util.getSerializableCompat
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class Broadcasts(private val context: Application) {
  sealed interface Event {
    data object ServiceRecreated : Event

    data object Started : Event

    data class Stopped(val cause: String?) : Event

    data object ProfileChanged : Event

    data class ProfileUpdateCompleted(val uuid: Uuid?) : Event

    data class ProfileUpdateFailed(val uuid: Uuid?, val reason: String?) : Event

    data object ProfileLoaded : Event
  }

  val clashRunningFlow: StateFlow<Boolean>
    field = MutableStateFlow(false)

  val event: SharedFlow<Event>
    field = MutableSharedFlow(extraBufferCapacity = 64)

  var clashRunning: Boolean
    get() = clashRunningFlow.value
    private set(value) {
      clashRunningFlow.value = value
    }

  private var registered = false
  private val broadcastReceiver =
    object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        val receivedContext = context ?: return
        val receivedIntent = intent ?: return
        if (receivedIntent.`package` != receivedContext.packageName) return

        val broadcastAction =
          tabbyRemoteBroadcastActionFromPlatformAction(
            action = receivedIntent.action,
            serviceRecreatedAction = Intents.ACTION_SERVICE_RECREATED,
            clashStartedAction = Intents.ACTION_CLASH_STARTED,
            clashStoppedAction = Intents.ACTION_CLASH_STOPPED,
            profileChangedAction = Intents.ACTION_PROFILE_CHANGED,
            profileUpdateCompletedAction = Intents.ACTION_PROFILE_UPDATE_COMPLETED,
            profileUpdateFailedAction = Intents.ACTION_PROFILE_UPDATE_FAILED,
            profileLoadedAction = Intents.ACTION_PROFILE_LOADED,
          )

        broadcastAction?.let { action ->
          tabbyRemoteBroadcastClashRunningState(action)?.let { clashRunning = it }
        }

        when (broadcastAction) {
          TabbyRemoteBroadcastAction.ServiceRecreated -> {
            event.tryEmit(Event.ServiceRecreated)
          }
          TabbyRemoteBroadcastAction.ClashStarted -> {
            event.tryEmit(Event.Started)
          }
          TabbyRemoteBroadcastAction.ClashStopped -> {
            event.tryEmit(Event.Stopped(receivedIntent.getStringExtra(Intents.EXTRA_STOP_REASON)))
          }
          TabbyRemoteBroadcastAction.ProfileChanged -> event.tryEmit(Event.ProfileChanged)
          TabbyRemoteBroadcastAction.ProfileUpdateCompleted ->
            event.tryEmit(
              Event.ProfileUpdateCompleted(receivedIntent.getSerializableCompat(Intents.EXTRA_UUID))
            )
          TabbyRemoteBroadcastAction.ProfileUpdateFailed ->
            event.tryEmit(
              Event.ProfileUpdateFailed(
                receivedIntent.getSerializableCompat(Intents.EXTRA_UUID),
                receivedIntent.getStringExtra(Intents.EXTRA_FAIL_REASON),
              )
            )
          TabbyRemoteBroadcastAction.ProfileLoaded -> event.tryEmit(Event.ProfileLoaded)
          null -> Unit
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
          addAction(Intents.ACTION_CLASH_STARTED)
          addAction(Intents.ACTION_CLASH_STOPPED)
          addAction(Intents.ACTION_PROFILE_CHANGED)
          addAction(Intents.ACTION_PROFILE_UPDATE_COMPLETED)
          addAction(Intents.ACTION_PROFILE_UPDATE_FAILED)
          addAction(Intents.ACTION_PROFILE_LOADED)
        },
      )
      registered = true

      clashRunning = StatusClient(context).currentProfile() != null
    } catch (e: Exception) {
      Log.w("Register global receiver: $e", e)
    }
  }
}
