package com.github.kr328.clash.app

import com.github.kr328.clash.common.store.Store
import com.github.kr328.clash.common.store.StoreProvider
import com.github.kr328.clash.core.model.AccessControlMode

private const val TUN_STACK_SYSTEM = "system"

data class TabbyNetworkSettings(
  val hasSystemProxyOption: Boolean = true,
  val enableVpn: Boolean = true,
  val bypassPrivateNetwork: Boolean = true,
  val dnsHijacking: Boolean = true,
  val allowBypass: Boolean = true,
  val allowIpv6: Boolean = false,
  val systemProxy: Boolean = true,
  val tunStackMode: String = TUN_STACK_SYSTEM,
  val accessControlMode: AccessControlMode = AccessControlMode.AcceptAll,
)

class TabbyNetworkSettingsRepository(
  private val uiStoreProvider: StoreProvider,
  private val serviceStoreProvider: StoreProvider,
) {
  private val uiStore = Store(uiStoreProvider)
  private val serviceStore = Store(serviceStoreProvider)

  private var storedEnableVpn by uiStore.boolean(ENABLE_VPN_KEY, true)
  private var storedBypassPrivateNetwork by serviceStore.boolean(BYPASS_PRIVATE_NETWORK_KEY, true)
  private var storedDnsHijacking by serviceStore.boolean(DNS_HIJACKING_KEY, true)
  private var storedAllowBypass by serviceStore.boolean(ALLOW_BYPASS_KEY, true)
  private var storedAllowIpv6 by serviceStore.boolean(ALLOW_IPV6_KEY, false)
  private var storedSystemProxy by serviceStore.boolean(SYSTEM_PROXY_KEY, true)
  private var storedTunStackMode by serviceStore.string(TUN_STACK_MODE_KEY, TUN_STACK_SYSTEM)
  private var storedAccessControlMode by
    serviceStore.enum(
      ACCESS_CONTROL_MODE_KEY,
      AccessControlMode.AcceptAll,
      AccessControlMode.entries.toTypedArray(),
    )

  fun query(defaults: TabbyNetworkSettings = TabbyNetworkSettings()): TabbyNetworkSettings {
    return TabbyNetworkSettings(
      hasSystemProxyOption = defaults.hasSystemProxyOption,
      enableVpn =
        if (uiStoreProvider.contains(ENABLE_VPN_KEY)) storedEnableVpn else defaults.enableVpn,
      bypassPrivateNetwork =
        if (serviceStoreProvider.contains(BYPASS_PRIVATE_NETWORK_KEY)) storedBypassPrivateNetwork
        else defaults.bypassPrivateNetwork,
      dnsHijacking =
        if (serviceStoreProvider.contains(DNS_HIJACKING_KEY)) storedDnsHijacking
        else defaults.dnsHijacking,
      allowBypass =
        if (serviceStoreProvider.contains(ALLOW_BYPASS_KEY)) storedAllowBypass
        else defaults.allowBypass,
      allowIpv6 =
        if (serviceStoreProvider.contains(ALLOW_IPV6_KEY)) storedAllowIpv6 else defaults.allowIpv6,
      systemProxy =
        if (serviceStoreProvider.contains(SYSTEM_PROXY_KEY)) storedSystemProxy
        else defaults.systemProxy,
      tunStackMode =
        if (serviceStoreProvider.contains(TUN_STACK_MODE_KEY)) storedTunStackMode
        else defaults.tunStackMode,
      accessControlMode =
        if (serviceStoreProvider.contains(ACCESS_CONTROL_MODE_KEY)) storedAccessControlMode
        else defaults.accessControlMode,
    )
  }

  fun setEnableVpn(value: Boolean) {
    storedEnableVpn = value
  }

  fun setBypassPrivateNetwork(value: Boolean) {
    storedBypassPrivateNetwork = value
  }

  fun setDnsHijacking(value: Boolean) {
    storedDnsHijacking = value
  }

  fun setAllowBypass(value: Boolean) {
    storedAllowBypass = value
  }

  fun setAllowIpv6(value: Boolean) {
    storedAllowIpv6 = value
  }

  fun setSystemProxy(value: Boolean) {
    storedSystemProxy = value
  }

  fun setTunStackMode(value: String) {
    storedTunStackMode = value
  }

  fun setAccessControlMode(value: AccessControlMode) {
    storedAccessControlMode = value
  }

  private companion object {
    const val ENABLE_VPN_KEY = "enable_vpn"
    const val BYPASS_PRIVATE_NETWORK_KEY = "bypass_private_network"
    const val DNS_HIJACKING_KEY = "dns_hijacking"
    const val ALLOW_BYPASS_KEY = "allow_bypass"
    const val ALLOW_IPV6_KEY = "allow_ipv6"
    const val SYSTEM_PROXY_KEY = "system_proxy"
    const val TUN_STACK_MODE_KEY = "tun_stack_mode"
    const val ACCESS_CONTROL_MODE_KEY = "access_control_mode"
  }
}
