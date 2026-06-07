package com.github.kr328.clash.proxy.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.engine.api.EngineController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import tabby.ui.proxy.generated.resources.Res
import tabby.ui.proxy.generated.resources.mode_switch_tips
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.unavailable

@Composable
fun ProxyRouteContent(
  engineController: EngineController,
  modifier: Modifier = Modifier,
  onReLaunch: () -> Unit = {},
  initialPreferences: ProxyRoutePreferences = ProxyRoutePreferences(),
  profileLoadedEvents: Flow<Unit> = emptyFlow(),
  onLastGroupChanged: (String) -> Unit = {},
  onExcludeNotSelectableChanged: (Boolean) -> Unit = {},
  onProxyLineChanged: (Int) -> Unit = {},
  onProxySortChanged: (ProxySort) -> Unit = {},
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  var uiState by
    remember(engineController, initialPreferences) {
      mutableStateOf(proxyInitialUiState(initialPreferences))
    }
  var selectedProxies by
    remember(engineController) { mutableStateOf(proxyInitialSelectedProxies()) }
  var initialized by remember(engineController) { mutableStateOf(false) }
  val modeSwitchTips = stringResource(Res.string.mode_switch_tips)
  val fallbackFailureMessage = stringResource(SharedRes.string.unavailable)

  suspend fun showFailureMessage(error: Throwable) {
    snackbarHostState.showSnackbar(error.message ?: fallbackFailureMessage)
  }

  suspend fun reload(index: Int) {
    when (val action = proxyReloadAction(uiState, index)) {
      is ProxyReloadAction.QueryGroup ->
        runCatching {
            val group = engineController.queryProxyGroup(action.groupName, action.sort)
            val sources =
              withContext(Dispatchers.Default) {
                group.toProxyItemSources(groupNames = action.groupNames)
              }

            proxyReloadResultAction(
              state = uiState,
              selectedProxies = selectedProxies,
              index = action.index,
              group = group,
              sources = sources,
            )
          }
          .onSuccess { result ->
            selectedProxies = result.selectedProxies
            uiState = result.state
          }
          .onFailure { showFailureMessage(it) }
      ProxyReloadAction.Ignore -> Unit
    }
  }

  suspend fun reloadAll() {
    proxyGroupReloadIndexes(uiState.groupNames).forEach { index -> reload(index) }
  }

  suspend fun fetchInitialState() {
    val action =
      runCatching {
          val mode = engineController.querySessionMode()
          val groupNames = engineController.queryProxyGroupNames(uiState.excludeNotSelectable)

          proxyInitialStateAction(
            state = uiState,
            overrideMode = mode,
            groupNames = groupNames,
            lastGroupName = initialPreferences.lastGroupName,
          )
        }
        .onFailure { showFailureMessage(it) }
        .getOrNull() ?: return

    uiState = action.state
    selectedProxies = action.selectedProxies
    initialized = true
    reloadAll()
  }

  suspend fun handleProfileLoadedEvent() {
    when (proxyBroadcastAction(ProxyBroadcastEventKind.ProfileLoaded, initialized)) {
      ProxyBroadcastAction.QueryGroupNames ->
        runCatching { engineController.queryProxyGroupNames(uiState.excludeNotSelectable) }
          .onSuccess { newNames ->
            when (proxyGroupNamesChangeAction(uiState.groupNames, newNames)) {
              ProxyGroupNamesChangeAction.ReLaunch -> onReLaunch()
              ProxyGroupNamesChangeAction.Ignore -> Unit
            }
          }
          .onFailure { showFailureMessage(it) }
      ProxyBroadcastAction.Ignore -> Unit
    }
  }

  LaunchedEffect(engineController, initialPreferences.lastGroupName) { fetchInitialState() }

  LaunchedEffect(engineController, profileLoadedEvents) {
    profileLoadedEvents.collect { handleProfileLoadedEvent() }
  }

  ProxyContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    uiState = uiState,
    selectedProxies = selectedProxies,
    onPageChanged = { index ->
      val action = proxyPageChangedAction(uiState, index)
      uiState = action.state

      when (val effect = action.effect) {
        is ProxyPageChangedEffect.SaveLastGroup -> onLastGroupChanged(effect.groupName)
        ProxyPageChangedEffect.Ignore -> Unit
      }
    },
    onUrlTest = { index ->
      scope.launch {
        val action = proxyUrlTestAction(uiState, index)
        uiState = action.state

        when (val effect = action.effect) {
          is ProxyUrlTestEffect.StartUrlTest ->
            runCatching {
                engineController.healthCheck(effect.groupName)
                reload(effect.index)
              }
              .onFailure { showFailureMessage(it) }
          ProxyUrlTestEffect.Ignore -> Unit
        }
      }
    },
    onExcludeNotSelectableChanged = { enabled ->
      scope.launch {
        onExcludeNotSelectableChanged(enabled)
        val action = proxyExcludeNotSelectableChangeAction(uiState, enabled)
        uiState = action.state

        when (action.effect) {
          ProxyPreferenceChangeEffect.ReLaunch -> onReLaunch()
          ProxyPreferenceChangeEffect.ReloadAll -> reloadAll()
        }
      }
    },
    onProxyLineChanged = { line ->
      scope.launch {
        onProxyLineChanged(line)
        val action = proxyLineChangeAction(uiState, line)
        uiState = action.state

        when (action.effect) {
          ProxyPreferenceChangeEffect.ReLaunch -> onReLaunch()
          ProxyPreferenceChangeEffect.ReloadAll -> reloadAll()
        }
      }
    },
    onProxySortChanged = { sort ->
      scope.launch {
        onProxySortChanged(sort)
        val action = proxySortChangeAction(uiState, sort)
        uiState = action.state

        when (action.effect) {
          ProxyPreferenceChangeEffect.ReLaunch -> onReLaunch()
          ProxyPreferenceChangeEffect.ReloadAll -> reloadAll()
        }
      }
    },
    onOverrideModeSelected = { mode ->
      scope.launch {
        val action = proxyOverrideModeAction(uiState, mode)
        uiState = action.state

        when (val effect = action.effect) {
          is ProxyOverrideModeEffect.ShowTipsAndPatchMode ->
            runCatching {
                snackbarHostState.showSnackbar(message = modeSwitchTips)
                engineController.patchSessionMode(effect.mode)
              }
              .onFailure { showFailureMessage(it) }
        }
      }
    },
    onProxySelected = { index, name ->
      scope.launch {
        when (val action = proxySelectedAction(uiState, index, name)) {
          is ProxySelectedAction.PatchSelector ->
            runCatching {
                engineController.patchSelector(action.groupName, action.proxyName)
                proxySelectedPatchAction(
                  state = uiState,
                  selectedProxies = selectedProxies,
                  index = action.index,
                  name = action.proxyName,
                )
              }
              .onSuccess { result ->
                selectedProxies = result.selectedProxies
                uiState = result.state
              }
              .onFailure { showFailureMessage(it) }
          ProxySelectedAction.Ignore -> Unit
        }
      }
    },
    onProxyDelayTest = { index, name ->
      scope.launch {
        val action = proxyDelayTestAction(uiState, index, name)
        uiState = action.state

        when (val effect = action.effect) {
          is ProxyDelayTestEffect.StartDelayTest ->
            try {
              runCatching {
                  engineController.healthCheckProxy(effect.groupName, effect.proxyName)
                  reload(effect.index)
                }
                .onFailure { showFailureMessage(it) }
            } finally {
              uiState =
                uiState.withProxyGroupState(effect.index) {
                  it.withDelayTestFinished(effect.proxyName)
                }
            }
          ProxyDelayTestEffect.Ignore -> Unit
        }
      }
    },
  )
}
