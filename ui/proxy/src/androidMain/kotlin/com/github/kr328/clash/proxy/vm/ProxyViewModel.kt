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
import com.github.kr328.clash.proxy.ui.ProxyDelayTestAction
import com.github.kr328.clash.proxy.ui.ProxyDelayTestEffect
import com.github.kr328.clash.proxy.ui.ProxyEventState
import com.github.kr328.clash.proxy.ui.ProxyGroupNamesChangeAction
import com.github.kr328.clash.proxy.ui.ProxyGroupSelectionAction
import com.github.kr328.clash.proxy.ui.ProxyGroupUiState
import com.github.kr328.clash.proxy.ui.ProxyOverrideModeAction
import com.github.kr328.clash.proxy.ui.ProxyOverrideModeEffect
import com.github.kr328.clash.proxy.ui.ProxyPreferenceChangeAction
import com.github.kr328.clash.proxy.ui.ProxyPreferenceChangeEffect
import com.github.kr328.clash.proxy.ui.ProxyProfileLoadedAction
import com.github.kr328.clash.proxy.ui.ProxyUiState
import com.github.kr328.clash.proxy.ui.ProxyUrlTestAction
import com.github.kr328.clash.proxy.ui.ProxyUrlTestEffect
import com.github.kr328.clash.proxy.ui.SelectedProxy
import com.github.kr328.clash.proxy.ui.initialSelectedProxies
import com.github.kr328.clash.proxy.ui.proxyDelayTestAction
import com.github.kr328.clash.proxy.ui.proxyExcludeNotSelectableChangeAction
import com.github.kr328.clash.proxy.ui.proxyGroupNamesChangeAction
import com.github.kr328.clash.proxy.ui.proxyGroupReloadIndexes
import com.github.kr328.clash.proxy.ui.proxyGroupSelectionAction
import com.github.kr328.clash.proxy.ui.proxyLineChangeAction
import com.github.kr328.clash.proxy.ui.proxyOverrideModeAction
import com.github.kr328.clash.proxy.ui.proxyProfileLoadedAction
import com.github.kr328.clash.proxy.ui.proxySortChangeAction
import com.github.kr328.clash.proxy.ui.proxyUrlTestAction
import com.github.kr328.clash.proxy.ui.toProxyItemSources
import com.github.kr328.clash.proxy.ui.withCurrentPage
import com.github.kr328.clash.proxy.ui.withDelayTestFinished
import com.github.kr328.clash.proxy.ui.withInitialProxyGroups
import com.github.kr328.clash.proxy.ui.withProxyGroup
import com.github.kr328.clash.proxy.ui.withProxyGroupState
import com.github.kr328.clash.proxy.ui.withProxyPreferences
import com.github.kr328.clash.proxy.ui.withProxySelectionRefreshed
import com.github.kr328.clash.proxy.ui.withSelectedProxy
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
            when (proxyProfileLoadedAction(initialized)) {
              ProxyProfileLoadedAction.QueryGroupNames -> {
                val newNames =
                  engineController.queryProxyGroupNames(uiStore.proxyExcludeNotSelectable)
                when (proxyGroupNamesChangeAction(uiState.value.groupNames, newNames)) {
                  ProxyGroupNamesChangeAction.ReLaunch ->
                    eventState.value = ProxyEventState.ReLaunch
                  ProxyGroupNamesChangeAction.Ignore -> Unit
                }
              }
              ProxyProfileLoadedAction.Ignore -> Unit
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
    when (val action = proxyGroupSelectionAction(names, index)) {
      is ProxyGroupSelectionAction.SelectGroup -> uiStore.proxyLastGroup = action.name
      ProxyGroupSelectionAction.Ignore -> Unit
    }
  }

  fun onExcludeNotSelectableChanged(enabled: Boolean) {
    uiStore.proxyExcludeNotSelectable = enabled
    applyProxyPreferenceChangeAction { proxyExcludeNotSelectableChangeAction(it, enabled) }
  }

  fun onProxyLineChanged(line: Int) {
    uiStore.proxyLine = line
    applyProxyPreferenceChangeAction { proxyLineChangeAction(it, line) }
  }

  fun onProxySortChanged(sort: ProxySort) {
    uiStore.proxySort = sort
    applyProxyPreferenceChangeAction { proxySortChangeAction(it, sort) }
  }

  fun onOverrideModeSelected(mode: TunnelState.Mode?) {
    applyProxyOverrideModeAction { proxyOverrideModeAction(it, mode) }
  }

  fun onUrlTest(index: Int) {
    applyProxyUrlTestAction { proxyUrlTestAction(it, index) }
  }

  fun onProxySelected(index: Int, name: String) {
    val groupName =
      when (val action = proxyGroupSelectionAction(uiState.value.groupNames, index)) {
        is ProxyGroupSelectionAction.SelectGroup -> action.name
        ProxyGroupSelectionAction.Ignore -> return
      }

    viewModelScope.launch {
      engineController.patchSelector(groupName, name)
      selectedProxies.update { it.withSelectedProxy(index, name) }
      updateGroupState(index) { it.withProxySelectionRefreshed() }
    }
  }

  fun onProxyDelayTest(index: Int, name: String) {
    applyProxyDelayTestAction { proxyDelayTestAction(it, index, name) }
  }

  fun reloadAll() {
    proxyGroupReloadIndexes(uiState.value.groupNames).forEach { idx -> reload(idx) }
  }

  private fun reload(index: Int) {
    viewModelScope.launch {
      val names = uiState.value.groupNames
      val groupName =
        when (val action = proxyGroupSelectionAction(names, index)) {
          is ProxyGroupSelectionAction.SelectGroup -> action.name
          ProxyGroupSelectionAction.Ignore -> return@launch
        }

      val sort = uiStore.proxySort

      val group = reloadLock.withPermit { engineController.queryProxyGroup(groupName, sort) }

      selectedProxies.update { it.withSelectedProxy(index, group.now) }

      val sources =
        withContext(Dispatchers.Default) { group.toProxyItemSources(groupNames = names) }

      updateGroupState(index) { it.withProxyGroup(group, sources) }
    }
  }

  private fun applyProxyPreferenceChangeAction(
    action: (ProxyUiState) -> ProxyPreferenceChangeAction
  ) {
    var effect: ProxyPreferenceChangeEffect? = null
    uiState.update { current ->
      val change = action(current)
      effect = change.effect
      change.state
    }
    applyProxyPreferenceChangeEffect(checkNotNull(effect))
  }

  private fun applyProxyPreferenceChangeEffect(effect: ProxyPreferenceChangeEffect) {
    when (effect) {
      ProxyPreferenceChangeEffect.ReLaunch -> eventState.value = ProxyEventState.ReLaunch
      ProxyPreferenceChangeEffect.ReloadAll -> reloadAll()
    }
  }

  private fun applyProxyOverrideModeAction(action: (ProxyUiState) -> ProxyOverrideModeAction) {
    var effect: ProxyOverrideModeEffect? = null
    uiState.update { current ->
      val change = action(current)
      effect = change.effect
      change.state
    }
    applyProxyOverrideModeEffect(checkNotNull(effect))
  }

  private fun applyProxyOverrideModeEffect(effect: ProxyOverrideModeEffect) {
    when (effect) {
      is ProxyOverrideModeEffect.ShowTipsAndPatchMode -> {
        eventState.value = ProxyEventState.ShowModeSwitchTips
        viewModelScope.launch { engineController.patchSessionMode(effect.mode) }
      }
    }
  }

  private fun applyProxyUrlTestAction(action: (ProxyUiState) -> ProxyUrlTestAction) {
    var effect: ProxyUrlTestEffect? = null
    uiState.update { current ->
      val change = action(current)
      effect = change.effect
      change.state
    }
    applyProxyUrlTestEffect(checkNotNull(effect))
  }

  private fun applyProxyUrlTestEffect(effect: ProxyUrlTestEffect) {
    when (effect) {
      is ProxyUrlTestEffect.StartUrlTest ->
        viewModelScope.launch {
          engineController.healthCheck(effect.groupName)
          reload(effect.index)
        }
      ProxyUrlTestEffect.Ignore -> Unit
    }
  }

  private fun applyProxyDelayTestAction(action: (ProxyUiState) -> ProxyDelayTestAction) {
    var effect: ProxyDelayTestEffect? = null
    uiState.update { current ->
      val change = action(current)
      effect = change.effect
      change.state
    }
    applyProxyDelayTestEffect(checkNotNull(effect))
  }

  private fun applyProxyDelayTestEffect(effect: ProxyDelayTestEffect) {
    when (effect) {
      is ProxyDelayTestEffect.StartDelayTest ->
        viewModelScope.launch {
          try {
            engineController.healthCheckProxy(effect.groupName, effect.proxyName)
            reload(effect.index)
          } finally {
            updateGroupState(effect.index) { it.withDelayTestFinished(effect.proxyName) }
          }
        }
      ProxyDelayTestEffect.Ignore -> Unit
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
