package com.github.kr328.clash.proxy.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.kr328.clash.engine.android.AndroidEngineController
import com.github.kr328.clash.glue.remote.Broadcasts
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.store.UiStore
import com.github.kr328.clash.settingsstore.TabbyProxyRoutePreferencesRepository
import kotlinx.coroutines.flow.map

@Composable
internal fun ProxyScreen(
  modifier: Modifier = Modifier,
  onReLaunch: () -> Unit,
) {
  val context = LocalContext.current
  val appContext = context.applicationContext
  val uiStore = remember(appContext) { UiStore(appContext) }
  val preferencesRepository =
    remember(uiStore) {
      SettingsStoreProxyRoutePreferencesRepository(
        TabbyProxyRoutePreferencesRepository(uiStore.storeProvider)
      )
    }
  val engineController = remember(appContext) { AndroidEngineController(appContext) }
  val broadcastEvents = remember {
    Remote.broadcasts.event.map { event -> event.toProxyBroadcastEventKind() }
  }

  ProxyRoutePreferencesRepositoryRouteContent(
    engineController = engineController,
    preferencesRepository = preferencesRepository,
    modifier = modifier,
    onReLaunch = onReLaunch,
    broadcastEvents = broadcastEvents,
  )
}

private fun Broadcasts.Event.toProxyBroadcastEventKind(): ProxyBroadcastEventKind =
  proxyBroadcastEventKindFromSource(toProxyBroadcastSourceEventKind())

private fun Broadcasts.Event.toProxyBroadcastSourceEventKind(): ProxyBroadcastSourceEventKind =
  when (this) {
    Broadcasts.Event.ProfileLoaded -> ProxyBroadcastSourceEventKind.ProfileLoaded
    Broadcasts.Event.ServiceRecreated -> ProxyBroadcastSourceEventKind.ServiceRecreated
    Broadcasts.Event.Started -> ProxyBroadcastSourceEventKind.Started
    is Broadcasts.Event.Stopped -> ProxyBroadcastSourceEventKind.Stopped
    Broadcasts.Event.ProfileChanged -> ProxyBroadcastSourceEventKind.ProfileChanged
    is Broadcasts.Event.ProfileUpdateCompleted ->
      ProxyBroadcastSourceEventKind.ProfileUpdateCompleted
    is Broadcasts.Event.ProfileUpdateFailed -> ProxyBroadcastSourceEventKind.ProfileUpdateFailed
  }
