package com.github.kr328.clash.common.service

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceNotificationModeTest {
  @Test
  fun selectsDynamicModeWhenDynamicNotificationIsEnabled() {
    assertEquals(
      TabbyServiceNotificationMode.Dynamic,
      tabbyServiceNotificationMode(dynamicNotification = true),
    )
  }

  @Test
  fun selectsStaticModeWhenDynamicNotificationIsDisabled() {
    assertEquals(
      TabbyServiceNotificationMode.Static,
      tabbyServiceNotificationMode(dynamicNotification = false),
    )
  }
}
