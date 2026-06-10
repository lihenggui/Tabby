package com.github.kr328.clash.common.network

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
