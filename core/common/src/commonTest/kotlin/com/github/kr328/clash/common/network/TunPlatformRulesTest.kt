package com.github.kr328.clash.common.network

import kotlin.test.Test
import kotlin.test.assertEquals

class TunPlatformRulesTest {
  @Test
  fun disablesConnectionOwnerUidLookupBeforeApi29() {
    assertEquals(
      false,
      tabbyTunCanQueryConnectionOwnerUid(TABBY_TUN_CONNECTION_OWNER_UID_MIN_SDK - 1),
    )
  }

  @Test
  fun enablesConnectionOwnerUidLookupFromApi29() {
    assertEquals(
      true,
      tabbyTunCanQueryConnectionOwnerUid(TABBY_TUN_CONNECTION_OWNER_UID_MIN_SDK),
    )
  }

  @Test
  fun keepsUnderlyingNetworksOverrideOnlyForLegacyVpnSdkRange() {
    assertEquals(
      false,
      tabbyTunShouldSetUnderlyingNetworks(TABBY_TUN_UNDERLYING_NETWORKS_MIN_SDK - 1),
    )
    assertEquals(
      true,
      tabbyTunShouldSetUnderlyingNetworks(TABBY_TUN_UNDERLYING_NETWORKS_MIN_SDK),
    )
    assertEquals(
      true,
      tabbyTunShouldSetUnderlyingNetworks(TABBY_TUN_UNDERLYING_NETWORKS_MAX_SDK),
    )
    assertEquals(
      false,
      tabbyTunShouldSetUnderlyingNetworks(TABBY_TUN_UNDERLYING_NETWORKS_MAX_SDK + 1),
    )
  }

  @Test
  fun enablesTunMeteredConfigurationFromApi29() {
    assertEquals(
      false,
      tabbyTunCanSetMetered(TABBY_TUN_METERED_MIN_SDK - 1),
    )
    assertEquals(
      true,
      tabbyTunCanSetMetered(TABBY_TUN_METERED_MIN_SDK),
    )
  }

  @Test
  fun enablesTunHttpProxyOnlyWhenPlatformAndSettingAllowIt() {
    assertEquals(
      false,
      tabbyTunCanSetHttpProxy(
        platformSdk = TABBY_TUN_HTTP_PROXY_MIN_SDK - 1,
        systemProxy = true,
      ),
    )
    assertEquals(
      false,
      tabbyTunCanSetHttpProxy(
        platformSdk = TABBY_TUN_HTTP_PROXY_MIN_SDK,
        systemProxy = false,
      ),
    )
    assertEquals(
      true,
      tabbyTunCanSetHttpProxy(
        platformSdk = TABBY_TUN_HTTP_PROXY_MIN_SDK,
        systemProxy = true,
      ),
    )
  }

  @Test
  fun buildsIpv4OnlyTunDeviceText() {
    assertEquals(
      TabbyTunDeviceText(
        gateway = "172.19.0.1/30",
        portal = "172.19.0.2",
        dns = "172.19.0.2",
      ),
      tabbyTunDeviceText(allowIpv6 = false, dnsHijacking = false),
    )
  }

  @Test
  fun appendsIpv6TunDeviceTextWhenIpv6IsAllowed() {
    assertEquals(
      TabbyTunDeviceText(
        gateway = "172.19.0.1/30,fdfe:dcba:9876::1/126",
        portal = "172.19.0.2,fdfe:dcba:9876::2",
        dns = "172.19.0.2,fdfe:dcba:9876::2",
      ),
      tabbyTunDeviceText(allowIpv6 = true, dnsHijacking = false),
    )
  }

  @Test
  fun usesAnyIpv4AddressForTunDnsWhenDnsHijackingIsEnabled() {
    assertEquals(
      TabbyTunDeviceText(
        gateway = "172.19.0.1/30,fdfe:dcba:9876::1/126",
        portal = "172.19.0.2,fdfe:dcba:9876::2",
        dns = "0.0.0.0",
      ),
      tabbyTunDeviceText(allowIpv6 = true, dnsHijacking = true),
    )
  }
}
