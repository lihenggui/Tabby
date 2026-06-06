package com.github.kr328.clash.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.AccessControlMode

@Composable
fun NetworkSettingsRouteContent(
  onStartAccessControlList: () -> Unit,
  modifier: Modifier = Modifier,
  clashRunning: Boolean = false,
  initialHasSystemProxyOption: Boolean = true,
  initialEnableVpn: Boolean = true,
  initialBypassPrivateNetwork: Boolean = true,
  initialDnsHijacking: Boolean = true,
  initialAllowBypass: Boolean = true,
  initialAllowIpv6: Boolean = false,
  initialSystemProxy: Boolean = true,
  initialTunStackMode: String = "system",
  initialAccessControlMode: AccessControlMode = AccessControlMode.AcceptAll,
  onEnableVpnChange: (Boolean) -> Unit = {},
  onBypassPrivateNetworkChange: (Boolean) -> Unit = {},
  onDnsHijackingChange: (Boolean) -> Unit = {},
  onAllowBypassChange: (Boolean) -> Unit = {},
  onAllowIpv6Change: (Boolean) -> Unit = {},
  onSystemProxyChange: (Boolean) -> Unit = {},
  onTunStackModeChange: (String) -> Unit = {},
  onAccessControlModeChange: (AccessControlMode) -> Unit = {},
) {
  var uiState by remember {
    mutableStateOf(
      networkSettingsInitialUiState(
        hasSystemProxyOption = initialHasSystemProxyOption,
        enableVpn = initialEnableVpn,
        bypassPrivateNetwork = initialBypassPrivateNetwork,
        dnsHijacking = initialDnsHijacking,
        allowBypass = initialAllowBypass,
        allowIpv6 = initialAllowIpv6,
        systemProxy = initialSystemProxy,
        tunStackMode = initialTunStackMode,
        accessControlMode = initialAccessControlMode,
      )
    )
  }

  NetworkSettingsContent(
    clashRunning = clashRunning,
    uiState = uiState,
    onEnableVpnChange = { value ->
      uiState = updateNetworkSettingsEnableVpn(uiState, value)
      onEnableVpnChange(value)
    },
    onBypassPrivateNetworkChange = { value ->
      uiState = updateNetworkSettingsBypassPrivateNetwork(uiState, value)
      onBypassPrivateNetworkChange(value)
    },
    onDnsHijackingChange = { value ->
      uiState = updateNetworkSettingsDnsHijacking(uiState, value)
      onDnsHijackingChange(value)
    },
    onAllowBypassChange = { value ->
      uiState = updateNetworkSettingsAllowBypass(uiState, value)
      onAllowBypassChange(value)
    },
    onAllowIpv6Change = { value ->
      uiState = updateNetworkSettingsAllowIpv6(uiState, value)
      onAllowIpv6Change(value)
    },
    onSystemProxyChange = { value ->
      uiState = updateNetworkSettingsSystemProxy(uiState, value)
      onSystemProxyChange(value)
    },
    onTunStackModeChange = { value ->
      uiState = updateNetworkSettingsTunStackMode(uiState, value)
      onTunStackModeChange(value)
    },
    onAccessControlModeChange = { value ->
      uiState = updateNetworkSettingsAccessControlMode(uiState, value)
      onAccessControlModeChange(value)
    },
    onAccessControlPackagesClick = onStartAccessControlList,
    modifier = modifier,
  )
}
