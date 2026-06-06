package com.github.kr328.clash.proxy.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.engine.android.AndroidEngineController
import com.github.kr328.clash.engine.api.EngineController
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.store.UiStore
import com.github.kr328.clash.proxy.ui.ProxyEventState
import com.github.kr328.clash.proxy.ui.ProxyGroupUiState
import com.github.kr328.clash.proxy.ui.ProxyUiState
import com.github.kr328.clash.proxy.ui.SelectedProxy
import com.github.kr328.clash.proxy.ui.initialSelectedProxies
import com.github.kr328.clash.proxy.ui.toProxyItemSources
import com.github.kr328.clash.proxy.ui.withCurrentPage
import com.github.kr328.clash.proxy.ui.withDelayTestFinished
import com.github.kr328.clash.proxy.ui.withDelayTestStarted
import com.github.kr328.clash.proxy.ui.withExcludeNotSelectable
import com.github.kr328.clash.proxy.ui.withInitialProxyGroups
import com.github.kr328.clash.proxy.ui.withOverrideMode
import com.github.kr328.clash.proxy.ui.withProxyGroup
import com.github.kr328.clash.proxy.ui.withProxyGroupState
import com.github.kr328.clash.proxy.ui.withProxyLine
import com.github.kr328.clash.proxy.ui.withProxyPreferences
import com.github.kr328.clash.proxy.ui.withProxySelectionRefreshed
import com.github.kr328.clash.proxy.ui.withProxySort
import com.github.kr328.clash.proxy.ui.withSelectedProxy
import com.github.kr328.clash.proxy.ui.withUrlTestStarted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
      it.withProxyPreferences(
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

    selectedProxies.value = initialSelectedProxies(names.size)
    uiState.update { it.withInitialProxyGroups(mode, names, uiStore.proxyLastGroup) }

    initialized = true
    reloadAll()
  }

  fun onPageChanged(index: Int) {
    val names = uiState.value.groupNames
    uiState.update { it.withCurrentPage(index) }
    names.getOrNull(index)?.let { uiStore.proxyLastGroup = it }
  }

  fun onExcludeNotSelectableChanged(enabled: Boolean) {
    uiStore.proxyExcludeNotSelectable = enabled
    uiState.update { it.withExcludeNotSelectable(enabled) }
    eventState.value = ProxyEventState.ReLaunch
  }

  fun onProxyLineChanged(line: Int) {
    uiStore.proxyLine = line
    uiState.update { it.withProxyLine(line) }
    reloadAll()
  }

  fun onProxySortChanged(sort: ProxySort) {
    uiStore.proxySort = sort
    uiState.update { it.withProxySort(sort) }
    reloadAll()
  }

  fun onOverrideModeSelected(mode: TunnelState.Mode?) {
    uiState.update { it.withOverrideMode(mode) }
    eventState.value = ProxyEventState.ShowModeSwitchTips
    viewModelScope.launch { engineController.patchSessionMode(mode) }
  }

  fun onUrlTest(index: Int) {
    val names = uiState.value.groupNames
    if (names.isEmpty() || index !in names.indices) return

    updateGroupState(index) { it.withUrlTestStarted() }

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
      selectedProxies.update { it.withSelectedProxy(index, name) }
      updateGroupState(index) { it.withProxySelectionRefreshed() }
    }
  }

  fun onProxyDelayTest(index: Int, name: String) {
    val names = uiState.value.groupNames
    if (index !in names.indices) return

    updateGroupState(index) { it.withDelayTestStarted(name) }

    viewModelScope.launch {
      try {
        engineController.healthCheckProxy(names[index], name)
        reload(index)
      } finally {
        updateGroupState(index) { it.withDelayTestFinished(name) }
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

      selectedProxies.update { it.withSelectedProxy(index, group.now) }

      val sources =
        withContext(Dispatchers.Default) { group.toProxyItemSources(groupNames = names) }

      updateGroupState(index) { it.withProxyGroup(group, sources) }
    }
  }

  private fun updateGroupState(
    index: Int,
    transform: (ProxyGroupUiState) -> ProxyGroupUiState,
  ) {
    uiState.update { current ->
      current.withProxyGroupState(index, transform)
    }
  }
}
