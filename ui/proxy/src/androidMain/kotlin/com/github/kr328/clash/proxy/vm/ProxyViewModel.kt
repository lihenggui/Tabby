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
import com.github.kr328.clash.proxy.ui.ProxyGroupUiState
import com.github.kr328.clash.proxy.ui.ProxyInitialStateAction
import com.github.kr328.clash.proxy.ui.ProxyOverrideModeAction
import com.github.kr328.clash.proxy.ui.ProxyOverrideModeEffect
import com.github.kr328.clash.proxy.ui.ProxyPageChangedAction
import com.github.kr328.clash.proxy.ui.ProxyPageChangedEffect
import com.github.kr328.clash.proxy.ui.ProxyPreferenceChangeAction
import com.github.kr328.clash.proxy.ui.ProxyPreferenceChangeEffect
import com.github.kr328.clash.proxy.ui.ProxyProfileLoadedAction
import com.github.kr328.clash.proxy.ui.ProxyReloadAction
import com.github.kr328.clash.proxy.ui.ProxySelectedAction
import com.github.kr328.clash.proxy.ui.ProxyUiState
import com.github.kr328.clash.proxy.ui.ProxyUrlTestAction
import com.github.kr328.clash.proxy.ui.ProxyUrlTestEffect
import com.github.kr328.clash.proxy.ui.SelectedProxy
import com.github.kr328.clash.proxy.ui.proxyDelayTestAction
import com.github.kr328.clash.proxy.ui.proxyExcludeNotSelectableChangeAction
import com.github.kr328.clash.proxy.ui.proxyGroupNamesChangeAction
import com.github.kr328.clash.proxy.ui.proxyGroupNamesChangeEventState
import com.github.kr328.clash.proxy.ui.proxyGroupReloadIndexes
import com.github.kr328.clash.proxy.ui.proxyInitialStateAction
import com.github.kr328.clash.proxy.ui.proxyLineChangeAction
import com.github.kr328.clash.proxy.ui.proxyOverrideModeAction
import com.github.kr328.clash.proxy.ui.proxyOverrideModeEventState
import com.github.kr328.clash.proxy.ui.proxyPageChangedAction
import com.github.kr328.clash.proxy.ui.proxyPreferenceChangeEventState
import com.github.kr328.clash.proxy.ui.proxyProfileLoadedAction
import com.github.kr328.clash.proxy.ui.proxyReloadAction
import com.github.kr328.clash.proxy.ui.proxyReloadSelectedProxies
import com.github.kr328.clash.proxy.ui.proxyReloadUiState
import com.github.kr328.clash.proxy.ui.proxySelectedAction
import com.github.kr328.clash.proxy.ui.proxySelectedProxies
import com.github.kr328.clash.proxy.ui.proxySelectedUiState
import com.github.kr328.clash.proxy.ui.proxySortChangeAction
import com.github.kr328.clash.proxy.ui.proxyUrlTestAction
import com.github.kr328.clash.proxy.ui.toProxyItemSources
import com.github.kr328.clash.proxy.ui.withDelayTestFinished
import com.github.kr328.clash.proxy.ui.withProxyGroupState
import com.github.kr328.clash.proxy.ui.withProxyPreferences
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
                proxyGroupNamesChangeEventState(
                    proxyGroupNamesChangeAction(uiState.value.groupNames, newNames)
                  )
                  ?.let { eventState.value = it }
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

    applyProxyInitialStateAction {
      proxyInitialStateAction(
        state = it,
        overrideMode = mode,
        groupNames = names,
        lastGroupName = uiStore.proxyLastGroup,
      )
    }

    initialized = true
    reloadAll()
  }

  fun onPageChanged(index: Int) {
    applyProxyPageChangedAction { proxyPageChangedAction(it, index) }
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
    when (val action = proxySelectedAction(uiState.value, index, name)) {
      is ProxySelectedAction.PatchSelector ->
        viewModelScope.launch {
          engineController.patchSelector(action.groupName, action.proxyName)
          applyProxySelectedPatch(action.index, action.proxyName)
        }
      ProxySelectedAction.Ignore -> Unit
    }
  }

  fun onProxyDelayTest(index: Int, name: String) {
    applyProxyDelayTestAction { proxyDelayTestAction(it, index, name) }
  }

  fun reloadAll() {
    proxyGroupReloadIndexes(uiState.value.groupNames).forEach { idx -> reload(idx) }
  }

  private fun reload(index: Int) {
    when (val action = proxyReloadAction(uiState.value, index)) {
      is ProxyReloadAction.QueryGroup ->
        viewModelScope.launch {
          val group = reloadLock.withPermit {
            engineController.queryProxyGroup(action.groupName, action.sort)
          }

          selectedProxies.update { current ->
            proxyReloadSelectedProxies(
              selectedProxies = current,
              index = action.index,
              group = group,
            )
          }

          val sources =
            withContext(Dispatchers.Default) {
              group.toProxyItemSources(groupNames = action.groupNames)
            }

          uiState.update { current ->
            proxyReloadUiState(
              state = current,
              index = action.index,
              group = group,
              sources = sources,
            )
          }
        }
      ProxyReloadAction.Ignore -> Unit
    }
  }

  private fun applyProxyInitialStateAction(action: (ProxyUiState) -> ProxyInitialStateAction) {
    selectedProxies.value = action(uiState.value).selectedProxies
    uiState.update { current -> action(current).state }
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

  private fun applyProxyPageChangedAction(action: (ProxyUiState) -> ProxyPageChangedAction) {
    var effect: ProxyPageChangedEffect? = null
    uiState.update { current ->
      val change = action(current)
      effect = change.effect
      change.state
    }
    applyProxyPageChangedEffect(checkNotNull(effect))
  }

  private fun applyProxyPageChangedEffect(effect: ProxyPageChangedEffect) {
    when (effect) {
      is ProxyPageChangedEffect.SaveLastGroup -> uiStore.proxyLastGroup = effect.groupName
      ProxyPageChangedEffect.Ignore -> Unit
    }
  }

  private fun applyProxyPreferenceChangeEffect(effect: ProxyPreferenceChangeEffect) {
    proxyPreferenceChangeEventState(effect)?.let { eventState.value = it }

    when (effect) {
      ProxyPreferenceChangeEffect.ReLaunch -> Unit
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
    eventState.value = proxyOverrideModeEventState(effect)

    when (effect) {
      is ProxyOverrideModeEffect.ShowTipsAndPatchMode -> {
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

  private fun applyProxySelectedPatch(index: Int, name: String) {
    selectedProxies.update { current ->
      proxySelectedProxies(
        selectedProxies = current,
        index = index,
        name = name,
      )
    }
    uiState.update { current -> proxySelectedUiState(current, index) }
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
