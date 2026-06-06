package com.github.kr328.clash.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.core.model.AccessControlMode
import com.github.kr328.clash.settings.vm.NetworkSettingsViewModel
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper

@Composable
internal fun NetworkSettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: NetworkSettingsViewModel = viewModel(),
  onStartAccessControlList: () -> Unit,
) {
  val clashRunning by viewModel.clashRunning.collectAsStateWithLifecycle()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  NetworkSettingsContent(
    clashRunning = clashRunning,
    uiState = uiState.toNetworkSettingsUiState(),
    onEnableVpnChange = viewModel::updateEnableVpn,
    onBypassPrivateNetworkChange = viewModel::updateBypassPrivateNetwork,
    onDnsHijackingChange = viewModel::updateDnsHijacking,
    onAllowBypassChange = viewModel::updateAllowBypass,
    onAllowIpv6Change = viewModel::updateAllowIpv6,
    onSystemProxyChange = viewModel::updateSystemProxy,
    onTunStackModeChange = viewModel::updateTunStackMode,
    onAccessControlModeChange = viewModel::updateAccessControlMode,
    onAccessControlPackagesClick = onStartAccessControlList,
    modifier = modifier,
  )
}

private fun NetworkSettingsViewModel.UiState.toNetworkSettingsUiState(): NetworkSettingsUiState {
  return NetworkSettingsUiState(
    hasSystemProxyOption = hasSystemProxyOption,
    enableVpn = enableVpn,
    bypassPrivateNetwork = bypassPrivateNetwork,
    dnsHijacking = dnsHijacking,
    allowBypass = allowBypass,
    allowIpv6 = allowIpv6,
    systemProxy = systemProxy,
    tunStackMode = tunStackMode,
    accessControlMode = accessControlMode,
  )
}

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun NetworkSettingsScreenPreview() {
  NetworkSettingsContent(
    clashRunning = false,
    uiState =
      NetworkSettingsUiState(
        hasSystemProxyOption = true,
        enableVpn = true,
        bypassPrivateNetwork = true,
        dnsHijacking = true,
        allowBypass = true,
        allowIpv6 = false,
        systemProxy = true,
        tunStackMode = "system",
        accessControlMode = AccessControlMode.AcceptAll,
      ),
    onEnableVpnChange = {},
    onBypassPrivateNetworkChange = {},
    onDnsHijackingChange = {},
    onAllowBypassChange = {},
    onAllowIpv6Change = {},
    onSystemProxyChange = {},
    onTunStackModeChange = {},
    onAccessControlModeChange = {},
    onAccessControlPackagesClick = {},
  )
}

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun NetworkSettingsScreenRunningPreview() {
  NetworkSettingsContent(
    clashRunning = true,
    uiState =
      NetworkSettingsUiState(
        hasSystemProxyOption = true,
        enableVpn = true,
        bypassPrivateNetwork = true,
        dnsHijacking = true,
        allowBypass = true,
        allowIpv6 = false,
        systemProxy = true,
        tunStackMode = "mixed",
        accessControlMode = AccessControlMode.DenySelected,
      ),
    onEnableVpnChange = {},
    onBypassPrivateNetworkChange = {},
    onDnsHijackingChange = {},
    onAllowBypassChange = {},
    onAllowIpv6Change = {},
    onSystemProxyChange = {},
    onTunStackModeChange = {},
    onAccessControlModeChange = {},
    onAccessControlPackagesClick = {},
  )
}
