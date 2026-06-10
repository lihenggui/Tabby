package com.github.kr328.clash.common.network

import com.github.kr328.clash.common.util.IPNet
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

  @Test
  fun returnsIpv4OnlyBypassPrivateRoutesWhenIpv6IsDisabled() {
    val routes = tabbyTunBypassPrivateRoutes(allowIpv6 = false)

    assertEquals(74, routes.size)
    assertEquals(IPNet.parse("1.0.0.0/8"), routes.first())
    assertEquals(IPNet.parse("255.255.255.254/32"), routes.last())
  }

  @Test
  fun appendsIpv6BypassPrivateRoutesWhenIpv6IsAllowed() {
    val routes = tabbyTunBypassPrivateRoutes(allowIpv6 = true)

    assertEquals(82, routes.size)
    assertEquals(IPNet.parse("::/1"), routes[74])
    assertEquals(IPNet.parse("fec0::/10"), routes.last())
  }

  @Test
  fun buildsHttpProxyExclusionListFromBypassPrivateNetworkSetting() {
    val remoteExclusions =
      listOf("*zhihu.com", "*zhimg.com", "*jd.com", "100ime-iat-api.xfyun.cn", "*360buyimg.com")

    assertEquals(
      remoteExclusions,
      tabbyTunHttpProxyExclusionList(bypassPrivateNetwork = false),
    )
    assertEquals(
      remoteExclusions +
        listOf(
          "localhost",
          "*.local",
          "127.*",
          "10.*",
          "172.16.*",
          "172.17.*",
          "172.18.*",
          "172.19.*",
          "172.2*",
          "172.30.*",
          "172.31.*",
          "192.168.*",
        ),
      tabbyTunHttpProxyExclusionList(bypassPrivateNetwork = true),
    )
  }

  @Test
  fun buildsIpv4OnlyTunNetworkPlanWithDefaultRoute() {
    assertEquals(
      TabbyTunNetworkPlan(
        addresses = listOf(IPNet.parse("172.19.0.1/30")),
        routes = listOf(IPNet.parse("0.0.0.0/0")),
        dnsServers = listOf("172.19.0.2"),
      ),
      tabbyTunNetworkPlan(allowIpv6 = false, bypassPrivateNetwork = false),
    )
  }

  @Test
  fun appendsIpv6TunNetworkPlanEntriesWhenIpv6IsAllowed() {
    assertEquals(
      TabbyTunNetworkPlan(
        addresses = listOf(IPNet.parse("172.19.0.1/30"), IPNet.parse("fdfe:dcba:9876::1/126")),
        routes = listOf(IPNet.parse("0.0.0.0/0"), IPNet.parse("::/0")),
        dnsServers = listOf("172.19.0.2", "fdfe:dcba:9876::2"),
      ),
      tabbyTunNetworkPlan(allowIpv6 = true, bypassPrivateNetwork = false),
    )
  }

  @Test
  fun buildsBypassPrivateTunNetworkPlanWithVirtualDnsRoutes() {
    val plan = tabbyTunNetworkPlan(allowIpv6 = true, bypassPrivateNetwork = true)

    assertEquals(
      listOf(IPNet.parse("172.19.0.1/30"), IPNet.parse("fdfe:dcba:9876::1/126")),
      plan.addresses,
    )
    assertEquals(84, plan.routes.size)
    assertEquals(IPNet.parse("1.0.0.0/8"), plan.routes.first())
    assertEquals(IPNet.parse("::/1"), plan.routes[74])
    assertEquals(IPNet.parse("172.19.0.2/32"), plan.routes[82])
    assertEquals(IPNet.parse("fdfe:dcba:9876::2/128"), plan.routes.last())
    assertEquals(listOf("172.19.0.2", "fdfe:dcba:9876::2"), plan.dnsServers)
  }
}
