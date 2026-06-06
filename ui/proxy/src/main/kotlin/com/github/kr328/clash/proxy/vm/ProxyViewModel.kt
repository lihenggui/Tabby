package com.github.kr328.clash.proxy.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.core.model.Proxy
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.engine.android.AndroidEngineController
import com.github.kr328.clash.engine.api.EngineController
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.store.UiStore
import com.github.kr328.clash.proxy.ui.ProxyEventState
import com.github.kr328.clash.proxy.ui.ProxyGroupUiState
import com.github.kr328.clash.proxy.ui.ProxyItemSource
import com.github.kr328.clash.proxy.ui.ProxyUiState
import com.github.kr328.clash.proxy.ui.SelectedProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

internal class ProxyViewModel(app: Application) : AndroidViewModel(app), DefaultLifecycleObserver {
  private val uiStore = UiStore(app)
  private val engineController: EngineController = AndroidEngineController(app)
  private var broadcastEventsJob: Job? = null
  private var fetchInitialStateJob: Job? = null
  @Volatile private var initialized = false
  // Allow up to 10 concurrent group queries to avoid overwhelming the service
  private val reloadLock = Semaphore(10)

  val uiState: StateFlow<ProxyUiState>
    field = MutableStateFlow(ProxyUiState())

  val eventState: StateFlow<ProxyEventState>
    field = MutableStateFlow<ProxyEventState>(ProxyEventState.Idle)

  val selectedProxies: StateFlow<List<SelectedProxy>>
    field = MutableStateFlow(emptyList())

  init {
    uiState.update {
      it.copy(
        proxyLine = uiStore.proxyLine,
        excludeNotSelectable = uiStore.proxyExcludeNotSelectable,
        proxySort = uiStore.proxySort,
      )
    }
  }

  override fun onStart(owner: LifecycleOwner) {
    broadcastEventsJob?.cancel()
    broadcastEventsJob = viewModelScope.launch {
      Remote.broadcasts.event.collect { event ->
        when (event) {
          ProfileLoaded -> {
            if (!initialized) return@collect
            val newNames = engineController.queryProxyGroupNames(uiStore.proxyExcludeNotSelectable)
            if (newNames != uiState.value.groupNames) {
              eventState.value = ProxyEventState.ReLaunch
            }
          }
          else -> Unit
        }
      }
    }

    fetchInitialStateJob?.cancel()
    fetchInitialStateJob = viewModelScope.launch { fetchInitialState() }
  }

  override fun onStop(owner: LifecycleOwner) {
    broadcastEventsJob?.cancel()
    broadcastEventsJob = null
    fetchInitialStateJob?.cancel()
    fetchInitialStateJob = null
  }

  fun consumeEvent() {
    eventState.value = ProxyEventState.Idle
  }

  private suspend fun fetchInitialState() {
    val mode = engineController.querySessionMode()
    val names = engineController.queryProxyGroupNames(uiStore.proxyExcludeNotSelectable)
    val preservedGroups =
      with(uiState.value) { groups.takeIf { groupNames == names && groups.size == names.size } }

    selectedProxies.value = List(names.size) { SelectedProxy("?") }

    val initialPage = names.indexOf(uiStore.proxyLastGroup).coerceAtLeast(0)
    val currentPage = initialPage.coerceAtMost((names.size - 1).coerceAtLeast(0))

    uiState.update {
      it.copy(
        overrideMode = mode,
        groupNames = names,
        groups = preservedGroups ?: List(names.size) { ProxyGroupUiState() },
        initialPage = initialPage,
        currentPage = currentPage,
      )
    }

    initialized = true
    reloadAll()
  }

  fun onPageChanged(index: Int) {
    val names = uiState.value.groupNames
    uiState.update { it.copy(currentPage = index) }
    names.getOrNull(index)?.let { uiStore.proxyLastGroup = it }
  }

  fun onExcludeNotSelectableChanged(enabled: Boolean) {
    uiStore.proxyExcludeNotSelectable = enabled
    uiState.update { it.copy(excludeNotSelectable = enabled) }
    eventState.value = ProxyEventState.ReLaunch
  }

  fun onProxyLineChanged(line: Int) {
    uiStore.proxyLine = line
    uiState.update { it.copy(proxyLine = line) }
    // Increment refresh version on groups
    uiState.update { current ->
      current.copy(groups = current.groups.map { it.copy(refreshVersion = it.refreshVersion + 1) })
    }
    reloadAll()
  }

  fun onProxySortChanged(sort: ProxySort) {
    uiStore.proxySort = sort
    uiState.update { it.copy(proxySort = sort) }
    reloadAll()
  }

  fun onOverrideModeSelected(mode: TunnelState.Mode?) {
    uiState.update { it.copy(overrideMode = mode) }
    eventState.value = ProxyEventState.ShowModeSwitchTips
    viewModelScope.launch { engineController.patchSessionMode(mode) }
  }

  fun onUrlTest(index: Int) {
    val names = uiState.value.groupNames
    if (names.isEmpty() || index !in names.indices) return

    updateGroupState(index) { it.copy(urlTesting = true) }

    viewModelScope.launch {
      engineController.healthCheck(names[index])
      reload(index)
    }
  }

  fun onProxySelected(index: Int, name: String) {
    val names = uiState.value.groupNames
    if (index !in names.indices) return

    viewModelScope.launch {
      engineController.patchSelector(names[index], name)
      selectedProxies.update { list ->
        list.toMutableList().apply { set(index, SelectedProxy(name)) }
      }
      // trigger refresh version to redraw
      updateGroupState(index) { it.copy(refreshVersion = it.refreshVersion + 1) }
    }
  }

  fun onProxyDelayTest(index: Int, name: String) {
    val names = uiState.value.groupNames
    if (index !in names.indices) return

    updateGroupState(index) {
      it.copy(delayTestingKeys = it.delayTestingKeys + name, refreshVersion = it.refreshVersion + 1)
    }

    viewModelScope.launch {
      try {
        engineController.healthCheckProxy(names[index], name)
        reload(index)
      } finally {
        updateGroupState(index) {
          it.copy(
            delayTestingKeys = it.delayTestingKeys - name,
            refreshVersion = it.refreshVersion + 1,
          )
        }
      }
    }
  }

  fun reloadAll() {
    val names = uiState.value.groupNames
    names.indices.forEach { idx -> reload(idx) }
  }

  private fun reload(index: Int) {
    viewModelScope.launch {
      val names = uiState.value.groupNames
      if (index !in names.indices) return@launch

      val sort = uiStore.proxySort

      val group = reloadLock.withPermit { engineController.queryProxyGroup(names[index], sort) }

      selectedProxies.update { list ->
        list.toMutableList().apply { set(index, SelectedProxy(group.now)) }
      }

      val sources =
        withContext(Dispatchers.Default) {
          val nameIndexMap = names.withIndex().associate { (index, name) -> name to index }
          group.proxies.map { proxy ->
            ProxyItemSource(
              proxy = proxy,
              linkIndex = if (proxy.type.group) nameIndexMap[proxy.name] ?: -1 else -1,
            )
          }
        }

      updateGroupState(index) {
        it.copy(
          selectable = group.type == Proxy.Type.Selector,
          urlTesting = false,
          sources = sources,
          delayTestingKeys =
            it.delayTestingKeys.intersect(sources.mapTo(mutableSetOf()) { s -> s.proxy.name }),
          refreshVersion = it.refreshVersion + 1,
        )
      }
    }
  }

  private fun updateGroupState(
    index: Int,
    transform: (ProxyGroupUiState) -> ProxyGroupUiState,
  ) {
    uiState.update { current ->
      if (index !in current.groups.indices) return@update current
      val newGroups = current.groups.toMutableList()
      newGroups[index] = transform(newGroups[index])
      current.copy(groups = newGroups)
    }
  }
}
