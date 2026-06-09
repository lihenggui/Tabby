package com.github.kr328.clash.proxy.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.engine.api.EngineController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface ProxyRoutePreferencesRepository {
  fun query(): ProxyRoutePreferences

  fun setLastGroupName(value: String)

  fun setExcludeNotSelectable(value: Boolean)

  fun setProxyLine(value: Int)

  fun setProxySort(value: ProxySort)
}

class InMemoryProxyRoutePreferencesRepository(
  initialPreferences: ProxyRoutePreferences = ProxyRoutePreferences()
) : ProxyRoutePreferencesRepository {
  private var preferences = initialPreferences

  override fun query(): ProxyRoutePreferences {
    return preferences
  }

  override fun setLastGroupName(value: String) {
    preferences = preferences.copy(lastGroupName = value)
  }

  override fun setExcludeNotSelectable(value: Boolean) {
    preferences = preferences.copy(excludeNotSelectable = value)
  }

  override fun setProxyLine(value: Int) {
    preferences = preferences.copy(proxyLine = value)
  }

  override fun setProxySort(value: ProxySort) {
    preferences = preferences.copy(proxySort = value)
  }
}

@Composable
fun ProxyRoutePreferencesRepositoryRouteContent(
  engineController: EngineController,
  preferencesRepository: ProxyRoutePreferencesRepository,
  modifier: Modifier = Modifier,
  onReLaunch: () -> Unit = {},
  broadcastEvents: Flow<ProxyBroadcastEventKind> = emptyFlow(),
  onLastGroupChanged: (String) -> Unit = {},
  onExcludeNotSelectableChanged: (Boolean) -> Unit = {},
  onProxyLineChanged: (Int) -> Unit = {},
  onProxySortChanged: (ProxySort) -> Unit = {},
) {
  val initialPreferences = remember(preferencesRepository) { preferencesRepository.query() }

  ProxyRouteContent(
    engineController = engineController,
    modifier = modifier,
    onReLaunch = onReLaunch,
    initialPreferences = initialPreferences,
    broadcastEvents = broadcastEvents,
    onLastGroupChanged = { value ->
      preferencesRepository.setLastGroupName(value)
      onLastGroupChanged(value)
    },
    onExcludeNotSelectableChanged = { value ->
      preferencesRepository.setExcludeNotSelectable(value)
      onExcludeNotSelectableChanged(value)
    },
    onProxyLineChanged = { value ->
      preferencesRepository.setProxyLine(value)
      onProxyLineChanged(value)
    },
    onProxySortChanged = { value ->
      preferencesRepository.setProxySort(value)
      onProxySortChanged(value)
    },
  )
}
