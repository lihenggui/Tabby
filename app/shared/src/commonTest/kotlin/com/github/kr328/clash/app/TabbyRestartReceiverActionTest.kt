package com.github.kr328.clash.app

import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyRestartReceiverActionTest {
  @Test
  fun tabbyRestartReceiverEventFromStringParsesKnownBroadcastActions() {
    assertEquals(
      TabbyRestartReceiverEvent.BootCompleted,
      tabbyRestartReceiverEventFromString(
        action = "boot-completed",
        bootCompletedBroadcastAction = "boot-completed",
        packageReplacedBroadcastAction = "package-replaced",
      ),
    )
    assertEquals(
      TabbyRestartReceiverEvent.PackageReplaced,
      tabbyRestartReceiverEventFromString(
        action = "package-replaced",
        bootCompletedBroadcastAction = "boot-completed",
        packageReplacedBroadcastAction = "package-replaced",
      ),
    )
  }

  @Test
  fun tabbyRestartReceiverEventFromStringIgnoresUnknownBroadcastActions() {
    assertEquals(
      null,
      tabbyRestartReceiverEventFromString(
        action = "unknown",
        bootCompletedBroadcastAction = "boot-completed",
        packageReplacedBroadcastAction = "package-replaced",
      ),
    )
    assertEquals(
      null,
      tabbyRestartReceiverEventFromString(
        action = null,
        bootCompletedBroadcastAction = "boot-completed",
        packageReplacedBroadcastAction = "package-replaced",
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
