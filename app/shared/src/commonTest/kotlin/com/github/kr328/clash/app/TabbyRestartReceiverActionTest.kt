package com.github.kr328.clash.app

import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyRestartReceiverActionTest {
  @Test
  fun tabbyRestartReceiverActionIgnoresUnknownEvents() {
    assertEquals(
      TabbyRestartReceiverAction.Ignore,
      tabbyRestartReceiverAction(
        event = null,
        shouldStartClashOnBoot = true,
      ),
    )
  }

  @Test
  fun tabbyRestartReceiverActionIgnoresKnownEventsWhenStartOnBootIsDisabled() {
    assertEquals(
      TabbyRestartReceiverAction.Ignore,
      tabbyRestartReceiverAction(
        event = TabbyRestartReceiverEvent.BootCompleted,
        shouldStartClashOnBoot = false,
      ),
    )
    assertEquals(
      TabbyRestartReceiverAction.Ignore,
      tabbyRestartReceiverAction(
        event = TabbyRestartReceiverEvent.PackageReplaced,
        shouldStartClashOnBoot = false,
      ),
    )
  }

  @Test
  fun tabbyRestartReceiverActionStartsKnownEventsWhenStartOnBootIsEnabled() {
    assertEquals(
      TabbyRestartReceiverAction.StartClash,
      tabbyRestartReceiverAction(
        event = TabbyRestartReceiverEvent.BootCompleted,
        shouldStartClashOnBoot = true,
      ),
    )
    assertEquals(
      TabbyRestartReceiverAction.StartClash,
      tabbyRestartReceiverAction(
        event = TabbyRestartReceiverEvent.PackageReplaced,
        shouldStartClashOnBoot = true,
      ),
    )
  }
}
