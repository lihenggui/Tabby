package com.github.kr328.clash.profile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.engine.android.AndroidEngineController
import com.github.kr328.clash.glue.remote.Broadcasts
import com.github.kr328.clash.glue.remote.Remote
import kotlinx.coroutines.flow.mapNotNull

@Composable
internal fun ProvidersScreen(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val appContext = context.applicationContext
  val engineController = remember(appContext) { AndroidEngineController(appContext) }
  val profileLoadedEvents = remember {
    Remote.broadcasts.event.mapNotNull { event -> event.toProfileLoadedSignal() }
  }

  EngineControllerProvidersRouteContent(
    engineController = engineController,
    modifier = modifier,
    profileLoadedEvents = profileLoadedEvents,
    onActionError = { cause -> Log.e("Provider action failed: ${cause.message}", cause) },
  )
}

private fun Broadcasts.Event.toProfileLoadedSignal(): Unit? {
  val event =
    when (this) {
      Broadcasts.Event.ServiceRecreated ->
        providersBroadcastEventFromPlatformPayload(ProvidersBroadcastEventKind.ServiceRecreated)
      Broadcasts.Event.Started ->
        providersBroadcastEventFromPlatformPayload(ProvidersBroadcastEventKind.Started)
      is Broadcasts.Event.Stopped ->
        providersBroadcastEventFromPlatformPayload(ProvidersBroadcastEventKind.Stopped)
      Broadcasts.Event.ProfileChanged ->
        providersBroadcastEventFromPlatformPayload(ProvidersBroadcastEventKind.ProfileChanged)
      is Broadcasts.Event.ProfileUpdateCompleted ->
        providersBroadcastEventFromPlatformPayload(
          ProvidersBroadcastEventKind.ProfileUpdateCompleted
        )
      is Broadcasts.Event.ProfileUpdateFailed ->
        providersBroadcastEventFromPlatformPayload(ProvidersBroadcastEventKind.ProfileUpdateFailed)
      Broadcasts.Event.ProfileLoaded ->
        providersBroadcastEventFromPlatformPayload(ProvidersBroadcastEventKind.ProfileLoaded)
    }

  return when (providersBroadcastAction(event)) {
    ProvidersBroadcastAction.FetchProviders -> Unit
    ProvidersBroadcastAction.Ignore -> null
  }
}
