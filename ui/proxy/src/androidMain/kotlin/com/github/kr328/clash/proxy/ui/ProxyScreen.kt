package com.github.kr328.clash.proxy.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.kr328.clash.engine.android.AndroidEngineController
import com.github.kr328.clash.glue.remote.Broadcasts
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.store.UiStore
import kotlinx.coroutines.flow.map

@Composable
internal fun ProxyScreen(
  modifier: Modifier = Modifier,
  onReLaunch: () -> Unit,
) {
  val context = LocalContext.current
  val appContext = context.applicationContext
  val uiStore = remember(appContext) { UiStore(appContext) }
  val engineController = remember(appContext) { AndroidEngineController(appContext) }
  val broadcastEvents = remember {
    Remote.broadcasts.event.map { event -> event.toProxyBroadcastEventKind() }
  }

  ProxyRouteContent(
    engineController = engineController,
    modifier = modifier,
    onReLaunch = onReLaunch,
    initialPreferences =
      ProxyRoutePreferences(
        proxyLine = uiStore.proxyLine,
        excludeNotSelectable = uiStore.proxyExcludeNotSelectable,
        proxySort = uiStore.proxySort,
        lastGroupName = uiStore.proxyLastGroup,
      ),
    broadcastEvents = broadcastEvents,
    onLastGroupChanged = { uiStore.proxyLastGroup = it },
    onExcludeNotSelectableChanged = { uiStore.proxyExcludeNotSelectable = it },
    onProxyLineChanged = { uiStore.proxyLine = it },
    onProxySortChanged = { uiStore.proxySort = it },
  )
}

private fun Broadcasts.Event.toProxyBroadcastEventKind(): ProxyBroadcastEventKind =
  when (this) {
    Broadcasts.Event.ProfileLoaded -> ProxyBroadcastEventKind.ProfileLoaded
    Broadcasts.Event.ServiceRecreated,
    Broadcasts.Event.Started,
    is Broadcasts.Event.Stopped,
    Broadcasts.Event.ProfileChanged,
    is Broadcasts.Event.ProfileUpdateCompleted,
    is Broadcasts.Event.ProfileUpdateFailed -> ProxyBroadcastEventKind.Other
  }
