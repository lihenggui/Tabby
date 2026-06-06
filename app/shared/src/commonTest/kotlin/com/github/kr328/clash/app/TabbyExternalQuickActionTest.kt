package com.github.kr328.clash.app

import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyExternalQuickActionTest {
  @Test
  fun tabbyExternalQuickActionPlanTogglesBasedOnCurrentRunningState() {
    assertEquals(
      TabbyExternalQuickActionPlan.StartClash,
      tabbyExternalQuickActionPlan(
        action = TabbyExternalQuickAction.ToggleClash,
        clashRunning = false,
      ),
    )
    assertEquals(
      TabbyExternalQuickActionPlan.StopClash,
      tabbyExternalQuickActionPlan(
        action = TabbyExternalQuickAction.ToggleClash,
        clashRunning = true,
      ),
    )
  }

  @Test
  fun tabbyExternalQuickActionPlanStartsOnlyWhenStopped() {
    assertEquals(
      TabbyExternalQuickActionPlan.StartClash,
      tabbyExternalQuickActionPlan(
        action = TabbyExternalQuickAction.StartClash,
        clashRunning = false,
      ),
    )
    assertEquals(
      TabbyExternalQuickActionPlan.ShowAlreadyStarted,
      tabbyExternalQuickActionPlan(
        action = TabbyExternalQuickAction.StartClash,
        clashRunning = true,
      ),
    )
  }

  @Test
  fun tabbyExternalQuickActionPlanStopsOnlyWhenRunning() {
    assertEquals(
      TabbyExternalQuickActionPlan.StopClash,
      tabbyExternalQuickActionPlan(
        action = TabbyExternalQuickAction.StopClash,
        clashRunning = true,
      ),
    )
    assertEquals(
      TabbyExternalQuickActionPlan.ShowAlreadyStopped,
      tabbyExternalQuickActionPlan(
        action = TabbyExternalQuickAction.StopClash,
        clashRunning = false,
      ),
    )
  }

  @Test
  fun tabbyStartClashResultActionShowsVpnPermissionWhenRequired() {
    assertEquals(
      TabbyStartClashResultAction.ShowVpnPermissionRequired,
      tabbyStartClashResultAction(vpnPermissionRequired = true),
    )
  }

  @Test
  fun tabbyStartClashResultActionShowsStartedWhenNoVpnPermissionIsRequired() {
    assertEquals(
      TabbyStartClashResultAction.ShowStarted,
      tabbyStartClashResultAction(vpnPermissionRequired = false),
    )
  }

  @Test
  fun tabbyStopClashResultActionShowsStopped() {
    assertEquals(TabbyStopClashResultAction.ShowStopped, tabbyStopClashResultAction())
  }
}
