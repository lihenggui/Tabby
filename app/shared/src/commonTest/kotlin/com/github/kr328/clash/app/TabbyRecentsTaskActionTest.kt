package com.github.kr328.clash.app

import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyRecentsTaskActionTest {
  @Test
  fun tabbyRecentsTaskActionExcludesTaskWhenAppShouldBeHidden() {
    assertEquals(
      TabbyRecentsTaskAction(excludeFromRecents = true),
      tabbyRecentsTaskAction(hideFromRecents = true),
    )
  }

  @Test
  fun tabbyRecentsTaskActionKeepsTaskInRecentsWhenAppShouldBeVisible() {
    assertEquals(
      TabbyRecentsTaskAction(excludeFromRecents = false),
      tabbyRecentsTaskAction(hideFromRecents = false),
    )
  }
}
