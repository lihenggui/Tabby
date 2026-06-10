package com.github.kr328.clash.network

import com.github.kr328.clash.core.model.ProfileSubscriptionUserInfo
import kotlin.test.Test
import kotlin.test.assertEquals

class SubscriptionUserInfoTest {
  @Test
  fun subscriptionUserInfoMapsToProfileSubscriptionUserInfo() {
    val subscriptionUserInfo =
      SubscriptionUserInfo(upload = 1024, download = 2048, total = 4096, expire = 8192)

    assertEquals(
      ProfileSubscriptionUserInfo(upload = 1024, download = 2048, total = 4096, expire = 8192),
      subscriptionUserInfo.toProfileSubscriptionUserInfo(),
    )
  }
}
