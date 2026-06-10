package com.github.kr328.clash.common.network

import com.github.kr328.clash.common.util.IPNet

const val TABBY_TUN_CONNECTION_OWNER_UID_MIN_SDK = 29
const val TABBY_TUN_UNDERLYING_NETWORKS_MIN_SDK = 22
const val TABBY_TUN_UNDERLYING_NETWORKS_MAX_SDK = 28
const val TABBY_TUN_METERED_MIN_SDK = 29
const val TABBY_TUN_HTTP_PROXY_MIN_SDK = 29
const val TABBY_TUN_IPV4_SUBNET_PREFIX = 30
const val TABBY_TUN_IPV4_GATEWAY = "172.19.0.1"
const val TABBY_TUN_IPV6_SUBNET_PREFIX = 126
const val TABBY_TUN_IPV6_GATEWAY = "fdfe:dcba:9876::1"
const val TABBY_TUN_IPV4_PORTAL = "172.19.0.2"
const val TABBY_TUN_IPV6_PORTAL = "fdfe:dcba:9876::2"
const val TABBY_TUN_IPV4_DNS = TABBY_TUN_IPV4_PORTAL
const val TABBY_TUN_IPV6_DNS = TABBY_TUN_IPV6_PORTAL
const val TABBY_TUN_ANY_IPV4_ADDRESS = "0.0.0.0"
const val TABBY_TUN_ANY_IPV6_ADDRESS = "::"

data class TabbyTunDeviceText(
  val gateway: String,
  val portal: String,
  val dns: String,
)

fun tabbyTunCanQueryConnectionOwnerUid(platformSdk: Int): Boolean {
  return platformSdk >= TABBY_TUN_CONNECTION_OWNER_UID_MIN_SDK
}

fun tabbyTunShouldSetUnderlyingNetworks(platformSdk: Int): Boolean {
  return platformSdk in TABBY_TUN_UNDERLYING_NETWORKS_MIN_SDK..TABBY_TUN_UNDERLYING_NETWORKS_MAX_SDK
}

fun tabbyTunCanSetMetered(platformSdk: Int): Boolean {
  return platformSdk >= TABBY_TUN_METERED_MIN_SDK
}

fun tabbyTunCanSetHttpProxy(platformSdk: Int, systemProxy: Boolean): Boolean {
  return systemProxy && platformSdk >= TABBY_TUN_HTTP_PROXY_MIN_SDK
}

fun tabbyTunDeviceText(allowIpv6: Boolean, dnsHijacking: Boolean): TabbyTunDeviceText {
  return TabbyTunDeviceText(
    gateway =
      "$TABBY_TUN_IPV4_GATEWAY/$TABBY_TUN_IPV4_SUBNET_PREFIX" +
        if (allowIpv6) ",$TABBY_TUN_IPV6_GATEWAY/$TABBY_TUN_IPV6_SUBNET_PREFIX" else "",
    portal = TABBY_TUN_IPV4_PORTAL + if (allowIpv6) ",$TABBY_TUN_IPV6_PORTAL" else "",
    dns =
      if (dnsHijacking) TABBY_TUN_ANY_IPV4_ADDRESS
      else TABBY_TUN_IPV4_DNS + if (allowIpv6) ",$TABBY_TUN_IPV6_DNS" else "",
  )
}

fun tabbyTunBypassPrivateRoutes(allowIpv6: Boolean): List<IPNet> {
  return TABBY_TUN_BYPASS_PRIVATE_ROUTE_V4 +
    if (allowIpv6) TABBY_TUN_BYPASS_PRIVATE_ROUTE_V6 else emptyList()
}

fun tabbyTunHttpProxyExclusionList(bypassPrivateNetwork: Boolean): List<String> {
  return TABBY_TUN_HTTP_PROXY_REMOTE_EXCLUSIONS +
    if (bypassPrivateNetwork) TABBY_TUN_HTTP_PROXY_LOCAL_EXCLUSIONS else emptyList()
}

/** Public IPv4 route set used when bypassing private and other special-use ranges. */
private val TABBY_TUN_BYPASS_PRIVATE_ROUTE_V4: List<IPNet> =
  listOf(
      "1.0.0.0/8",
      "2.0.0.0/7",
      "4.0.0.0/6",
      "8.0.0.0/7",
      "11.0.0.0/8",
      "12.0.0.0/6",
      "16.0.0.0/4",
      "32.0.0.0/3",
      "64.0.0.0/3",
      "96.0.0.0/4",
      "112.0.0.0/5",
      "120.0.0.0/6",
      "124.0.0.0/7",
      "126.0.0.0/8",
      "128.0.0.0/3",
      "160.0.0.0/5",
      "168.0.0.0/8",
      "169.0.0.0/9",
      "169.128.0.0/10",
      "169.192.0.0/11",
      "169.224.0.0/12",
      "169.240.0.0/13",
      "169.248.0.0/14",
      "169.252.0.0/15",
      "169.255.0.0/16",
      "170.0.0.0/7",
      "172.0.0.0/12",
      "172.32.0.0/11",
      "172.64.0.0/10",
      "172.128.0.0/9",
      "173.0.0.0/8",
      "174.0.0.0/7",
      "176.0.0.0/4",
      "192.0.0.0/9",
      "192.128.0.0/11",
      "192.160.0.0/13",
      "192.169.0.0/16",
      "192.170.0.0/15",
      "192.172.0.0/14",
      "192.176.0.0/12",
      "192.192.0.0/10",
      "193.0.0.0/8",
      "194.0.0.0/7",
      "196.0.0.0/6",
      "200.0.0.0/5",
      "208.0.0.0/4",
      "240.0.0.0/5",
      "248.0.0.0/6",
      "252.0.0.0/7",
      "254.0.0.0/8",
      "255.0.0.0/9",
      "255.128.0.0/10",
      "255.192.0.0/11",
      "255.224.0.0/12",
      "255.240.0.0/13",
      "255.248.0.0/14",
      "255.252.0.0/15",
      "255.254.0.0/16",
      "255.255.0.0/17",
      "255.255.128.0/18",
      "255.255.192.0/19",
      "255.255.224.0/20",
      "255.255.240.0/21",
      "255.255.248.0/22",
      "255.255.252.0/23",
      "255.255.254.0/24",
      "255.255.255.0/25",
      "255.255.255.128/26",
      "255.255.255.192/27",
      "255.255.255.224/28",
      "255.255.255.240/29",
      "255.255.255.248/30",
      "255.255.255.252/31",
      "255.255.255.254/32",
    )
    .map(IPNet::parse)

/** Exclude fc00::/7, fe80::/10, ff00::/8. */
private val TABBY_TUN_BYPASS_PRIVATE_ROUTE_V6: List<IPNet> =
  listOf(
      "::/1",
      "8000::/2",
      "c000::/3",
      "e000::/4",
      "f000::/5",
      "f800::/6",
      "fe00::/9",
      "fec0::/10",
    )
    .map(IPNet::parse)

private val TABBY_TUN_HTTP_PROXY_LOCAL_EXCLUSIONS: List<String> =
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
  )

private val TABBY_TUN_HTTP_PROXY_REMOTE_EXCLUSIONS: List<String> =
  listOf("*zhihu.com", "*zhimg.com", "*jd.com", "100ime-iat-api.xfyun.cn", "*360buyimg.com")
