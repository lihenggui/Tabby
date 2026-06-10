package com.github.kr328.clash.settings.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import com.github.kr328.clash.core.model.AccessControlMode
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineVpnLock
import com.github.kr328.clash.ui.icon.TabbyIcons
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tabby.ui.settings.generated.resources.Res
import tabby.ui.settings.generated.resources.access_control_mode
import tabby.ui.settings.generated.resources.access_control_packages
import tabby.ui.settings.generated.resources.access_control_packages_summary
import tabby.ui.settings.generated.resources.allow_all_apps
import tabby.ui.settings.generated.resources.allow_bypass
import tabby.ui.settings.generated.resources.allow_bypass_summary
import tabby.ui.settings.generated.resources.allow_ipv6
import tabby.ui.settings.generated.resources.allow_ipv6_summary
import tabby.ui.settings.generated.resources.allow_selected_apps
import tabby.ui.settings.generated.resources.bypass_private_network
import tabby.ui.settings.generated.resources.bypass_private_network_summary
import tabby.ui.settings.generated.resources.deny_selected_apps
import tabby.ui.settings.generated.resources.dns_hijacking
import tabby.ui.settings.generated.resources.dns_hijacking_summary
import tabby.ui.settings.generated.resources.network
import tabby.ui.settings.generated.resources.options_unavailable
import tabby.ui.settings.generated.resources.route_system_traffic
import tabby.ui.settings.generated.resources.routing_via_vpn_service
import tabby.ui.settings.generated.resources.system_proxy
import tabby.ui.settings.generated.resources.system_proxy_summary
import tabby.ui.settings.generated.resources.tun_stack_gvisor
import tabby.ui.settings.generated.resources.tun_stack_mixed
import tabby.ui.settings.generated.resources.tun_stack_mode
import tabby.ui.settings.generated.resources.tun_stack_system
import tabby.ui.settings.generated.resources.vpn_service_options

internal data class NetworkSettingsUiState(
  val hasSystemProxyOption: Boolean,
  val enableVpn: Boolean,
  val bypassPrivateNetwork: Boolean,
  val dnsHijacking: Boolean,
  val allowBypass: Boolean,
  val allowIpv6: Boolean,
  val systemProxy: Boolean,
  val tunStackMode: String,
  val accessControlMode: AccessControlMode,
)

internal fun networkSettingsInitialUiState(
  hasSystemProxyOption: Boolean,
  enableVpn: Boolean,
  bypassPrivateNetwork: Boolean,
  dnsHijacking: Boolean,
  allowBypass: Boolean,
  allowIpv6: Boolean,
  systemProxy: Boolean,
  tunStackMode: String,
  accessControlMode: AccessControlMode,
): NetworkSettingsUiState {
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

@Composable
internal fun NetworkSettingsContent(
  clashRunning: Boolean,
  uiState: NetworkSettingsUiState,
  onEnableVpnChange: (Boolean) -> Unit,
  onBypassPrivateNetworkChange: (Boolean) -> Unit,
  onDnsHijackingChange: (Boolean) -> Unit,
  onAllowBypassChange: (Boolean) -> Unit,
  onAllowIpv6Change: (Boolean) -> Unit,
  onSystemProxyChange: (Boolean) -> Unit,
  onTunStackModeChange: (String) -> Unit,
  onAccessControlModeChange: (AccessControlMode) -> Unit,
  onAccessControlPackagesClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val barMessage = stringResource(Res.string.options_unavailable)

  LaunchedEffect(clashRunning) {
    if (clashRunning) {
      snackbarHostState.showSnackbar(
        message = barMessage,
        withDismissAction = true,
        duration = SnackbarDuration.Indefinite,
      )
    }
  }

  val vpnDependenciesEnabled = !clashRunning && uiState.enableVpn
  val tunStackMode = TunStackMode.fromValue(uiState.tunStackMode)

  TabbyScaffold(
    title = stringResource(Res.string.network),
    modifier = modifier,
    snackbarHostState = snackbarHostState,
  ) { innerPadding ->
    ProvidePreferenceLocals {
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        item(key = "route_system_traffic") {
          SwitchPreference(
            value = uiState.enableVpn,
            onValueChange = onEnableVpnChange,
            enabled = !clashRunning,
            icon = { Icon(imageVector = TabbyIcons.BaselineVpnLock, contentDescription = null) },
            title = { Text(stringResource(Res.string.route_system_traffic)) },
            summary = { Text(stringResource(Res.string.routing_via_vpn_service)) },
          )
        }
        preferenceCategory(
          key = "cat_vpn_service_options",
          title = { Text(stringResource(Res.string.vpn_service_options)) },
        )
        item(key = "bypass_private_network") {
          SwitchPreference(
            value = uiState.bypassPrivateNetwork,
            onValueChange = onBypassPrivateNetworkChange,
            enabled = vpnDependenciesEnabled,
            title = { Text(stringResource(Res.string.bypass_private_network)) },
            summary = { Text(stringResource(Res.string.bypass_private_network_summary)) },
          )
        }
        item(key = "dns_hijacking") {
          SwitchPreference(
            value = uiState.dnsHijacking,
            onValueChange = onDnsHijackingChange,
            enabled = vpnDependenciesEnabled,
            title = { Text(stringResource(Res.string.dns_hijacking)) },
            summary = { Text(stringResource(Res.string.dns_hijacking_summary)) },
          )
        }
        item(key = "allow_bypass") {
          SwitchPreference(
            value = uiState.allowBypass,
            onValueChange = onAllowBypassChange,
            enabled = vpnDependenciesEnabled,
            title = { Text(stringResource(Res.string.allow_bypass)) },
            summary = { Text(stringResource(Res.string.allow_bypass_summary)) },
          )
        }
        item(key = "allow_ipv6") {
          SwitchPreference(
            value = uiState.allowIpv6,
            onValueChange = onAllowIpv6Change,
            enabled = vpnDependenciesEnabled,
            title = { Text(stringResource(Res.string.allow_ipv6)) },
            summary = { Text(stringResource(Res.string.allow_ipv6_summary)) },
          )
        }
        if (uiState.hasSystemProxyOption) {
          item(key = "system_proxy") {
            SwitchPreference(
              value = uiState.systemProxy,
              onValueChange = onSystemProxyChange,
              enabled = vpnDependenciesEnabled,
              title = { Text(stringResource(Res.string.system_proxy)) },
              summary = { Text(stringResource(Res.string.system_proxy_summary)) },
            )
          }
        }
        item(key = "tun_stack_mode") {
          ListPreference(
            value = tunStackMode,
            onValueChange = { onTunStackModeChange(it.persistedValue) },
            values = TunStackMode.entries,
            enabled = vpnDependenciesEnabled,
            title = { Text(stringResource(Res.string.tun_stack_mode)) },
            summary = { Text(stringResource(tunStackMode.summaryStringResource)) },
            valueToText = { AnnotatedString(stringResource(it.summaryStringResource)) },
          )
        }
        item(key = "access_control_mode") {
          ListPreference(
            value = uiState.accessControlMode,
            onValueChange = onAccessControlModeChange,
            values =
              listOf(
                AccessControlMode.AcceptAll,
                AccessControlMode.AcceptSelected,
                AccessControlMode.DenySelected,
              ),
            enabled = vpnDependenciesEnabled,
            title = { Text(stringResource(Res.string.access_control_mode)) },
            summary = { Text(stringResource(uiState.accessControlMode.summaryStringResource)) },
            valueToText = { AnnotatedString(stringResource(it.summaryStringResource)) },
          )
        }
        preference(
          key = "access_control_packages",
          title = { Text(stringResource(Res.string.access_control_packages)) },
          summary = { Text(stringResource(Res.string.access_control_packages_summary)) },
          onClick = onAccessControlPackagesClick,
        )
      }
    }
  }
}

private enum class TunStackMode(val persistedValue: String) {
  System("system"),
  Gvisor("gvisor"),
  Mixed("mixed");

  companion object {
    fun fromValue(value: String): TunStackMode {
      return entries.firstOrNull { it.persistedValue == value } ?: System
    }
  }
}

private val TunStackMode.summaryStringResource: StringResource
  get() =
    when (this) {
      TunStackMode.System -> Res.string.tun_stack_system
      TunStackMode.Gvisor -> Res.string.tun_stack_gvisor
      TunStackMode.Mixed -> Res.string.tun_stack_mixed
    }

private val AccessControlMode.summaryStringResource: StringResource
  get() =
    when (this) {
      AccessControlMode.AcceptAll -> Res.string.allow_all_apps
      AccessControlMode.AcceptSelected -> Res.string.allow_selected_apps
      AccessControlMode.DenySelected -> Res.string.deny_selected_apps
    }
