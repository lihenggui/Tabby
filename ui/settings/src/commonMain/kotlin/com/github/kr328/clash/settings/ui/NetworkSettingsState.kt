package com.github.kr328.clash.settings.ui

import com.github.kr328.clash.core.model.AccessControlMode

internal fun updateNetworkSettingsEnableVpn(
  uiState: NetworkSettingsUiState,
  enableVpn: Boolean,
): NetworkSettingsUiState {
  return uiState.copy(enableVpn = enableVpn)
}

internal fun updateNetworkSettingsBypassPrivateNetwork(
  uiState: NetworkSettingsUiState,
  bypassPrivateNetwork: Boolean,
): NetworkSettingsUiState {
  return uiState.copy(bypassPrivateNetwork = bypassPrivateNetwork)
}

internal fun updateNetworkSettingsDnsHijacking(
  uiState: NetworkSettingsUiState,
  dnsHijacking: Boolean,
): NetworkSettingsUiState {
  return uiState.copy(dnsHijacking = dnsHijacking)
}

internal fun updateNetworkSettingsAllowBypass(
  uiState: NetworkSettingsUiState,
  allowBypass: Boolean,
): NetworkSettingsUiState {
  return uiState.copy(allowBypass = allowBypass)
}

internal fun updateNetworkSettingsAllowIpv6(
  uiState: NetworkSettingsUiState,
  allowIpv6: Boolean,
): NetworkSettingsUiState {
  return uiState.copy(allowIpv6 = allowIpv6)
}

internal fun updateNetworkSettingsSystemProxy(
  uiState: NetworkSettingsUiState,
  systemProxy: Boolean,
): NetworkSettingsUiState {
  return uiState.copy(systemProxy = systemProxy)
}

internal fun updateNetworkSettingsTunStackMode(
  uiState: NetworkSettingsUiState,
  tunStackMode: String,
): NetworkSettingsUiState {
  return uiState.copy(tunStackMode = tunStackMode)
}

internal fun updateNetworkSettingsAccessControlMode(
  uiState: NetworkSettingsUiState,
  accessControlMode: AccessControlMode,
): NetworkSettingsUiState {
  return uiState.copy(accessControlMode = accessControlMode)
}
