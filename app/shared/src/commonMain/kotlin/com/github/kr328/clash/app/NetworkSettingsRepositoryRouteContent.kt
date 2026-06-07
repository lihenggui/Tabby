package com.github.kr328.clash.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.AccessControlMode
import com.github.kr328.clash.settings.ui.NetworkSettingsRouteContent

@Composable
internal fun NetworkSettingsRepositoryRouteContent(
  repository: TabbyNetworkSettingsRepository,
  onStartAccessControlList: () -> Unit,
  modifier: Modifier = Modifier,
  clashRunning: Boolean = false,
  defaults: TabbyNetworkSettings = TabbyNetworkSettings(),
  onEnableVpnChange: (Boolean) -> Unit = {},
  onBypassPrivateNetworkChange: (Boolean) -> Unit = {},
  onDnsHijackingChange: (Boolean) -> Unit = {},
  onAllowBypassChange: (Boolean) -> Unit = {},
  onAllowIpv6Change: (Boolean) -> Unit = {},
  onSystemProxyChange: (Boolean) -> Unit = {},
  onTunStackModeChange: (String) -> Unit = {},
  onAccessControlModeChange: (AccessControlMode) -> Unit = {},
) {
  val settings = remember(repository, defaults) { repository.query(defaults) }

  NetworkSettingsRouteContent(
    onStartAccessControlList = onStartAccessControlList,
    modifier = modifier,
    clashRunning = clashRunning,
    initialHasSystemProxyOption = settings.hasSystemProxyOption,
    initialEnableVpn = settings.enableVpn,
    initialBypassPrivateNetwork = settings.bypassPrivateNetwork,
    initialDnsHijacking = settings.dnsHijacking,
    initialAllowBypass = settings.allowBypass,
    initialAllowIpv6 = settings.allowIpv6,
    initialSystemProxy = settings.systemProxy,
    initialTunStackMode = settings.tunStackMode,
    initialAccessControlMode = settings.accessControlMode,
    onEnableVpnChange = { value ->
      repository.setEnableVpn(value)
      onEnableVpnChange(value)
    },
    onBypassPrivateNetworkChange = { value ->
      repository.setBypassPrivateNetwork(value)
      onBypassPrivateNetworkChange(value)
    },
    onDnsHijackingChange = { value ->
      repository.setDnsHijacking(value)
      onDnsHijackingChange(value)
    },
    onAllowBypassChange = { value ->
      repository.setAllowBypass(value)
      onAllowBypassChange(value)
    },
    onAllowIpv6Change = { value ->
      repository.setAllowIpv6(value)
      onAllowIpv6Change(value)
    },
    onSystemProxyChange = { value ->
      repository.setSystemProxy(value)
      onSystemProxyChange(value)
    },
    onTunStackModeChange = { value ->
      repository.setTunStackMode(value)
      onTunStackModeChange(value)
    },
    onAccessControlModeChange = { value ->
      repository.setAccessControlMode(value)
      onAccessControlModeChange(value)
    },
  )
}
