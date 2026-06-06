package com.github.kr328.clash.app

import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyExternalQuickActionTest {
  @Test
  fun tabbyExternalQuickActionStringMapsActionsToPlatformStrings() {
    assertEquals(
      "toggle",
      tabbyExternalQuickActionString(
        action = TabbyExternalQuickAction.ToggleClash,
        toggleClashAction = "toggle",
        startClashAction = "start",
        stopClashAction = "stop",
      ),
    )
    assertEquals(
      "start",
      tabbyExternalQuickActionString(
        action = TabbyExternalQuickAction.StartClash,
        toggleClashAction = "toggle",
        startClashAction = "start",
        stopClashAction = "stop",
      ),
    )
    assertEquals(
      "stop",
      tabbyExternalQuickActionString(
        action = TabbyExternalQuickAction.StopClash,
        toggleClashAction = "toggle",
        startClashAction = "start",
        stopClashAction = "stop",
      ),
    )
  }

  @Test
  fun tabbyExternalQuickActionFromStringParsesKnownPlatformStrings() {
    assertEquals(
      TabbyExternalQuickAction.ToggleClash,
      tabbyExternalQuickActionFromString(
        action = "toggle",
        toggleClashAction = "toggle",
        startClashAction = "start",
        stopClashAction = "stop",
      ),
    )
    assertEquals(
      TabbyExternalQuickAction.StartClash,
      tabbyExternalQuickActionFromString(
        action = "start",
        toggleClashAction = "toggle",
        startClashAction = "start",
        stopClashAction = "stop",
      ),
    )
    assertEquals(
      TabbyExternalQuickAction.StopClash,
      tabbyExternalQuickActionFromString(
        action = "stop",
        toggleClashAction = "toggle",
        startClashAction = "start",
        stopClashAction = "stop",
      ),
    )
  }

  @Test
  fun tabbyExternalQuickActionFromStringIgnoresUnknownPlatformStrings() {
    assertEquals(
      null,
      tabbyExternalQuickActionFromString(
        action = "unknown",
        toggleClashAction = "toggle",
        startClashAction = "start",
        stopClashAction = "stop",
      ),
    )
    assertEquals(
      null,
      tabbyExternalQuickActionFromString(
        action = null,
        toggleClashAction = "toggle",
        startClashAction = "start",
        stopClashAction = "stop",
      ),
    )
  }

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
  fun tabbyExternalQuickActionHandlingPlanHandlesKnownActions() {
    assertEquals(
      TabbyExternalQuickActionHandlingPlan.Handle(TabbyExternalQuickActionPlan.StartClash),
      tabbyExternalQuickActionHandlingPlan(
        action = TabbyExternalQuickAction.ToggleClash,
        clashRunning = false,
      ),
    )
    assertEquals(
      TabbyExternalQuickActionHandlingPlan.Handle(TabbyExternalQuickActionPlan.ShowAlreadyStarted),
      tabbyExternalQuickActionHandlingPlan(
        action = TabbyExternalQuickAction.StartClash,
        clashRunning = true,
      ),
    )
  }

  @Test
  fun tabbyExternalQuickActionHandlingPlanIgnoresUnknownActions() {
    assertEquals(
      TabbyExternalQuickActionHandlingPlan.Ignore,
      tabbyExternalQuickActionHandlingPlan(
        action = null,
        clashRunning = true,
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

  @Test
  fun tabbyExternalQuickActionPlanFeedbackResourceMapsFeedbackOnlyPlans() {
    assertEquals(
      1,
      tabbyExternalQuickActionPlanFeedbackResource(
        plan = TabbyExternalQuickActionPlan.ShowAlreadyStarted,
        alreadyStartedResource = 1,
        alreadyStoppedResource = 2,
      ),
    )
    assertEquals(
      2,
      tabbyExternalQuickActionPlanFeedbackResource(
        plan = TabbyExternalQuickActionPlan.ShowAlreadyStopped,
        alreadyStartedResource = 1,
        alreadyStoppedResource = 2,
      ),
    )
    assertEquals(
      null,
      tabbyExternalQuickActionPlanFeedbackResource(
        plan = TabbyExternalQuickActionPlan.StartClash,
        alreadyStartedResource = 1,
        alreadyStoppedResource = 2,
      ),
    )
    assertEquals(
      null,
      tabbyExternalQuickActionPlanFeedbackResource(
        plan = TabbyExternalQuickActionPlan.StopClash,
        alreadyStartedResource = 1,
        alreadyStoppedResource = 2,
      ),
    )
  }

  @Test
  fun tabbyStartClashResultFeedbackResourceMapsResultActions() {
    assertEquals(
      1,
      tabbyStartClashResultFeedbackResource(
        action = TabbyStartClashResultAction.ShowVpnPermissionRequired,
        vpnPermissionRequiredResource = 1,
        startedResource = 2,
      ),
    )
    assertEquals(
      2,
      tabbyStartClashResultFeedbackResource(
        action = TabbyStartClashResultAction.ShowStarted,
        vpnPermissionRequiredResource = 1,
        startedResource = 2,
      ),
    )
  }

  @Test
  fun tabbyStopClashResultFeedbackResourceMapsResultAction() {
    assertEquals(
      1,
      tabbyStopClashResultFeedbackResource(
        action = TabbyStopClashResultAction.ShowStopped,
        stoppedResource = 1,
      ),
    )
  }
}
