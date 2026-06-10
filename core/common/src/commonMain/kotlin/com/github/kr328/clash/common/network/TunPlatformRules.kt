package com.github.kr328.clash.common.network

const val TABBY_TUN_CONNECTION_OWNER_UID_MIN_SDK = 29
const val TABBY_TUN_UNDERLYING_NETWORKS_MIN_SDK = 22
const val TABBY_TUN_UNDERLYING_NETWORKS_MAX_SDK = 28
const val TABBY_TUN_METERED_MIN_SDK = 29
const val TABBY_TUN_HTTP_PROXY_MIN_SDK = 29

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
