package com.github.kr328.clash.app

import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyDialerReceiverActionTest {
  @Test
  fun tabbyDialerReceiverActionOpensMainActivity() {
    assertEquals(TabbyDialerReceiverAction.OpenMainActivity, tabbyDialerReceiverAction())
  }

  @Test
  fun tabbyDialerReceiverMainActivityLaunchOptionsOpenInNewTask() {
    assertEquals(
      TabbyDialerReceiverMainActivityLaunchOptions(openInNewTask = true),
      tabbyDialerReceiverMainActivityLaunchOptions(TabbyDialerReceiverAction.OpenMainActivity),
    )
  }

  @Test
  fun tabbyDialerReceiverMainActivityLaunchFlagsMapsNewTaskFlag() {
    assertEquals(
      4,
      tabbyDialerReceiverMainActivityLaunchFlags(
        launchOptions = TabbyDialerReceiverMainActivityLaunchOptions(openInNewTask = true),
        openInNewTaskFlag = 4,
      ),
    )
    assertEquals(
      0,
      tabbyDialerReceiverMainActivityLaunchFlags(
        launchOptions = TabbyDialerReceiverMainActivityLaunchOptions(openInNewTask = false),
        openInNewTaskFlag = 4,
      ),
    )
  }
}
