package com.github.kr328.clash.proxy.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.proxy.vm.ProxyViewModel
import com.github.kr328.clash.ui.lifecycle.viewModelWithLifecycle
import org.jetbrains.compose.resources.stringResource
import tabby.ui.proxy.generated.resources.Res
import tabby.ui.proxy.generated.resources.mode_switch_tips

@Composable
internal fun ProxyScreen(
  modifier: Modifier = Modifier,
  viewModel: ProxyViewModel = viewModelWithLifecycle(),
  onReLaunch: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val selectedProxies by viewModel.selectedProxies.collectAsStateWithLifecycle()
  val eventState by viewModel.eventState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val modeSwitchTips = stringResource(Res.string.mode_switch_tips)

  LaunchedEffect(eventState) {
    when (proxyEventPlatformAction(eventState)) {
      ProxyEventPlatformAction.Ignore -> Unit
      ProxyEventPlatformAction.ReLaunch -> {
        onReLaunch()
      }
      ProxyEventPlatformAction.ShowModeSwitchTips -> {
        snackbarHostState.showSnackbar(message = modeSwitchTips)
      }
    }
    viewModel.consumeEvent()
  }

  ProxyContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    uiState = uiState,
    selectedProxies = selectedProxies,
    onPageChanged = viewModel::onPageChanged,
    onUrlTest = viewModel::onUrlTest,
    onExcludeNotSelectableChanged = viewModel::onExcludeNotSelectableChanged,
    onProxyLineChanged = viewModel::onProxyLineChanged,
    onProxySortChanged = viewModel::onProxySortChanged,
    onOverrideModeSelected = viewModel::onOverrideModeSelected,
    onProxySelected = viewModel::onProxySelected,
    onProxyDelayTest = viewModel::onProxyDelayTest,
  )
}
