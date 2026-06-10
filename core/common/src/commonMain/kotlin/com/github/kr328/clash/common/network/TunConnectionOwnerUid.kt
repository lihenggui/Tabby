package com.github.kr328.clash.common.network

const val TABBY_TUN_CONNECTION_OWNER_UID_MIN_SDK = 29

fun tabbyTunCanQueryConnectionOwnerUid(platformSdk: Int): Boolean {
  return platformSdk >= TABBY_TUN_CONNECTION_OWNER_UID_MIN_SDK
}
