package com.github.kr328.clash.proxy.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.kr328.clash.core.model.ProxySort
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
  val preferencesRepository =
    remember(appContext) { AndroidProxyRoutePreferencesRepository(UiStore(appContext)) }
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

private class AndroidProxyRoutePreferencesRepository(private val uiStore: UiStore) :
  ProxyRoutePreferencesRepository {
  override fun query(): ProxyRoutePreferences {
    return ProxyRoutePreferences(
      proxyLine = uiStore.proxyLine,
      excludeNotSelectable = uiStore.proxyExcludeNotSelectable,
      proxySort = uiStore.proxySort,
      lastGroupName = uiStore.proxyLastGroup,
    )
  }

  override fun setLastGroupName(value: String) {
    uiStore.proxyLastGroup = value
  }

  override fun setExcludeNotSelectable(value: Boolean) {
    uiStore.proxyExcludeNotSelectable = value
  }

  override fun setProxyLine(value: Int) {
    uiStore.proxyLine = value
  }

  override fun setProxySort(value: ProxySort) {
    uiStore.proxySort = value
  }
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
