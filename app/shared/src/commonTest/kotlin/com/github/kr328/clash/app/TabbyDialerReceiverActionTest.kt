package com.github.kr328.clash.app

import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyDialerReceiverActionTest {
  @Test
  fun tabbyDialerReceiverActionOpensMainActivity() {
    assertEquals(TabbyDialerReceiverAction.OpenMainActivity, tabbyDialerReceiverAction())
  }
}
