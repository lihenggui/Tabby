package com.github.kr328.clash.settings.ui

import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.core.model.TunnelState

internal val booleanOptions: List<Boolean?> = listOf(null, true, false)

internal interface OverrideSettingsActions {
  fun updateHttpPort(value: Int?) = Unit

  fun updateSocksPort(value: Int?) = Unit

  fun updateRedirectPort(value: Int?) = Unit

  fun updateTproxyPort(value: Int?) = Unit

  fun updateMixedPort(value: Int?) = Unit

  fun updateAuthentication(value: List<String>?) = Unit

  fun updateAllowLan(value: Boolean?) = Unit

  fun updateIpv6(value: Boolean?) = Unit

  fun updateBindAddress(value: String?) = Unit

  fun updateExternalController(value: String?) = Unit

  fun updateExternalControllerTls(value: String?) = Unit

  fun updateAllowOrigins(value: List<String>?) = Unit

  fun updateAllowPrivateNetwork(value: Boolean?) = Unit

  fun updateSecret(value: String?) = Unit

  fun updateMode(value: TunnelState.Mode?) = Unit

  fun updateLogLevel(value: LogMessage.Level?) = Unit

  fun updateHosts(value: Map<String, String>?) = Unit

  fun updateDnsEnable(value: Boolean?) = Unit

  fun updateDnsPreferH3(value: Boolean?) = Unit

  fun updateDnsListen(value: String?) = Unit

  fun updateAppendSystemDns(value: Boolean?) = Unit

  fun updateDnsIpv6(value: Boolean?) = Unit

  fun updateDnsUseHosts(value: Boolean?) = Unit

  fun updateDnsEnhancedMode(value: ConfigurationOverride.DnsEnhancedMode?) = Unit

  fun updateDnsNameServer(value: List<String>?) = Unit

  fun updateDnsFallback(value: List<String>?) = Unit

  fun updateDnsDefaultServer(value: List<String>?) = Unit

  fun updateDnsFakeIpFilter(value: List<String>?) = Unit

  fun updateDnsFakeIpFilterMode(value: ConfigurationOverride.FilterMode?) = Unit

  fun updateDnsGeoIpFallback(value: Boolean?) = Unit

  fun updateDnsGeoIpCode(value: String?) = Unit

  fun updateDnsDomainFallback(value: List<String>?) = Unit

  fun updateDnsIpcidrFallback(value: List<String>?) = Unit

  fun updateDnsNameserverPolicy(value: Map<String, String>?) = Unit
}
