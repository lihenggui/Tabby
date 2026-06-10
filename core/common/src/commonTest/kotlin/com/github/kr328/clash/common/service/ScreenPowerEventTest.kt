package com.github.kr328.clash.common.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScreenPowerEventTest {
  private val screenOnAction = "screen-action-on"
  private val screenOffAction = "screen-action-off"

  @Test
  fun mapsPlatformScreenActionsToCommonEvents() {
    assertEquals(
      TabbyScreenPowerEvent.ScreenOn,
      tabbyScreenPowerEventFromPlatformAction(
        action = screenOnAction,
        screenOnAction = screenOnAction,
        screenOffAction = screenOffAction,
      ),
    )
    assertEquals(
      TabbyScreenPowerEvent.ScreenOff,
      tabbyScreenPowerEventFromPlatformAction(
        action = screenOffAction,
        screenOnAction = screenOnAction,
        screenOffAction = screenOffAction,
      ),
    )
  }

  @Test
  fun rejectsMissingOrUnknownPlatformScreenActions() {
    assertNull(
      tabbyScreenPowerEventFromPlatformAction(
        action = null,
        screenOnAction = screenOnAction,
        screenOffAction = screenOffAction,
      )
    )
    assertNull(
      tabbyScreenPowerEventFromPlatformAction(
        action = "power-action-low",
        screenOnAction = screenOnAction,
        screenOffAction = screenOffAction,
      )
    )
  }

  @Test
  fun exposesInteractiveStateForScreenPowerEvents() {
    assertTrue(TabbyScreenPowerEvent.ScreenOn.isInteractive)
    assertFalse(TabbyScreenPowerEvent.ScreenOff.isInteractive)
  }

  @Test
  fun mapsInteractiveStateToSuspendDecision() {
    assertFalse(tabbyShouldSuspendCoreForInteractiveState(isInteractive = true))
    assertTrue(tabbyShouldSuspendCoreForInteractiveState(isInteractive = false))
  }
}
