package com.github.kr328.clash.common.service

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceNotificationProfileTitleTest {
  @Test
  fun usesProfileNameWhenAvailable() {
    assertEquals(
      "Work",
      tabbyServiceNotificationProfileTitle(profileName = "Work", defaultTitle = "Tabby"),
    )
  }

  @Test
  fun fallsBackWhenProfileNameIsMissing() {
    assertEquals(
      "Not selected",
      tabbyServiceNotificationProfileTitle(profileName = null, defaultTitle = "Not selected"),
    )
  }

  @Test
  fun preservesEmptyProfileName() {
    assertEquals(
      "",
      tabbyServiceNotificationProfileTitle(profileName = "", defaultTitle = "Not selected"),
    )
  }
}
