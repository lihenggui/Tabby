package com.github.kr328.clash.app

import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyRestartReceiverActionTest {
  @Test
  fun tabbyRestartReceiverEventFromStringParsesKnownPlatformActions() {
    assertEquals(
      TabbyRestartReceiverEvent.BootCompleted,
      tabbyRestartReceiverEventFromString(
        action = "boot-completed",
        bootCompletedAction = "boot-completed",
        packageReplacedAction = "package-replaced",
      ),
    )
    assertEquals(
      TabbyRestartReceiverEvent.PackageReplaced,
      tabbyRestartReceiverEventFromString(
        action = "package-replaced",
        bootCompletedAction = "boot-completed",
        packageReplacedAction = "package-replaced",
      ),
    )
  }

  @Test
  fun tabbyRestartReceiverEventFromStringIgnoresUnknownPlatformActions() {
    assertEquals(
      null,
      tabbyRestartReceiverEventFromString(
        action = "unknown",
        bootCompletedAction = "boot-completed",
        packageReplacedAction = "package-replaced",
      ),
    )
    assertEquals(
      null,
      tabbyRestartReceiverEventFromString(
        action = null,
        bootCompletedAction = "boot-completed",
        packageReplacedAction = "package-replaced",
      ),
    )
  }

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
