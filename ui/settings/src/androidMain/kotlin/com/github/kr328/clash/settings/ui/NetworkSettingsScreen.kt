package com.github.kr328.clash.settings.ui

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.core.model.AccessControlMode
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.store.UiStore
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper

@Composable
internal fun NetworkSettingsScreen(
  modifier: Modifier = Modifier,
  onStartAccessControlList: () -> Unit,
) {
  val context = LocalContext.current
  val appContext = context.applicationContext
  val uiStore = remember(appContext) { UiStore(appContext) }
  val serviceStore = remember(appContext) { ServiceStore(appContext) }
  val clashRunning by Remote.broadcasts.clashRunningFlow.collectAsStateWithLifecycle()

  NetworkSettingsRouteContent(
    onStartAccessControlList = onStartAccessControlList,
    modifier = modifier,
    clashRunning = clashRunning,
    initialHasSystemProxyOption = networkSettingsHasSystemProxyOption(),
    initialEnableVpn = uiStore.enableVpn,
    initialBypassPrivateNetwork = serviceStore.bypassPrivateNetwork,
    initialDnsHijacking = serviceStore.dnsHijacking,
    initialAllowBypass = serviceStore.allowBypass,
    initialAllowIpv6 = serviceStore.allowIpv6,
    initialSystemProxy = serviceStore.systemProxy,
    initialTunStackMode = serviceStore.tunStackMode,
    initialAccessControlMode = serviceStore.accessControlMode,
    onEnableVpnChange = { value -> uiStore.enableVpn = value },
    onBypassPrivateNetworkChange = { value -> serviceStore.bypassPrivateNetwork = value },
    onDnsHijackingChange = { value -> serviceStore.dnsHijacking = value },
    onAllowBypassChange = { value -> serviceStore.allowBypass = value },
    onAllowIpv6Change = { value -> serviceStore.allowIpv6 = value },
    onSystemProxyChange = { value -> serviceStore.systemProxy = value },
    onTunStackModeChange = { value -> serviceStore.tunStackMode = value },
    onAccessControlModeChange = { value -> serviceStore.accessControlMode = value },
  )
}

private fun networkSettingsHasSystemProxyOption(): Boolean =
  Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

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
