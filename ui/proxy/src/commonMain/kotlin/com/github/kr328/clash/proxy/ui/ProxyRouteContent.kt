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
import com.github.kr328.clash.engine.api.EngineController
import kotlinx.coroutines.Dispatchers
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
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  var uiState by remember(engineController) { mutableStateOf(proxyInitialUiState()) }
  var selectedProxies by
    remember(engineController) { mutableStateOf(proxyInitialSelectedProxies()) }
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
            lastGroupName = "",
          )
        }
        .onFailure { showFailureMessage(it) }
        .getOrNull() ?: return

    uiState = action.state
    selectedProxies = action.selectedProxies
    reloadAll()
  }

  LaunchedEffect(engineController) { fetchInitialState() }

  ProxyContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    uiState = uiState,
    selectedProxies = selectedProxies,
    onPageChanged = { index ->
      val action = proxyPageChangedAction(uiState, index)
      uiState = action.state
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
