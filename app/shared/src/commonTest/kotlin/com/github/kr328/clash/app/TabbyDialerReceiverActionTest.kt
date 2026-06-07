package com.github.kr328.clash.app

import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyDialerReceiverActionTest {
  @Test
  fun tabbyDialerReceiverActionOpensMainActivity() {
    assertEquals(TabbyDialerReceiverAction.OpenMainActivity, tabbyDialerReceiverAction())
  }

  @Test
  fun tabbyDialerReceiverPlatformSpecLaunchesMainActivityInNewTask() {
    assertEquals(
      TabbyDialerReceiverPlatformSpec(intentFlags = 0b100),
      tabbyDialerReceiverPlatformSpec(
        action = TabbyDialerReceiverAction.OpenMainActivity,
        openInNewTaskFlag = 0b100,
      ),
    )
  }
}
