package com.github.kr328.clash.profile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.engine.android.AndroidEngineController
import com.github.kr328.clash.glue.remote.Broadcasts
import com.github.kr328.clash.glue.remote.Remote
import kotlinx.coroutines.flow.map

@Composable
internal fun ProvidersScreen(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val appContext = context.applicationContext
  val engineController = remember(appContext) { AndroidEngineController(appContext) }
  val broadcastEvents = remember {
    Remote.broadcasts.event.map { event -> event.toProvidersEvent() }
  }

  EngineControllerProvidersRouteContent(
    engineController = engineController,
    modifier = modifier,
    broadcastEvents = broadcastEvents,
    onActionError = { cause -> Log.e("Provider action failed: ${cause.message}", cause) },
  )
}

private fun Broadcasts.Event.toProvidersEvent(): ProvidersBroadcastEvent =
  when (this) {
    Broadcasts.Event.ServiceRecreated ->
      providersBroadcastEventFromSource(ProvidersBroadcastSourceEventKind.ServiceRecreated)
    Broadcasts.Event.Started ->
      providersBroadcastEventFromSource(ProvidersBroadcastSourceEventKind.Started)
    is Broadcasts.Event.Stopped ->
      providersBroadcastEventFromSource(ProvidersBroadcastSourceEventKind.Stopped)
    Broadcasts.Event.ProfileChanged ->
      providersBroadcastEventFromSource(ProvidersBroadcastSourceEventKind.ProfileChanged)
    is Broadcasts.Event.ProfileUpdateCompleted ->
      providersBroadcastEventFromSource(ProvidersBroadcastSourceEventKind.ProfileUpdateCompleted)
    is Broadcasts.Event.ProfileUpdateFailed ->
      providersBroadcastEventFromSource(ProvidersBroadcastSourceEventKind.ProfileUpdateFailed)
    Broadcasts.Event.ProfileLoaded ->
      providersBroadcastEventFromSource(ProvidersBroadcastSourceEventKind.ProfileLoaded)
  }
