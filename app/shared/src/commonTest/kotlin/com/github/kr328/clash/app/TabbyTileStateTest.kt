package com.github.kr328.clash.app

import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyTileStateTest {
  @Test
  fun tabbyTileBroadcastActionFromStringParsesKnownBroadcastActions() {
    assertEquals(
      TabbyTileBroadcastAction.ClashStarted,
      tabbyTileBroadcastActionFromString(
        action = "clash-started",
        clashStartedAction = "clash-started",
        clashStoppedAction = "clash-stopped",
        serviceRecreatedAction = "service-recreated",
        profileLoadedAction = "profile-loaded",
      ),
    )
    assertEquals(
      TabbyTileBroadcastAction.ClashStopped,
      tabbyTileBroadcastActionFromString(
        action = "clash-stopped",
        clashStartedAction = "clash-started",
        clashStoppedAction = "clash-stopped",
        serviceRecreatedAction = "service-recreated",
        profileLoadedAction = "profile-loaded",
      ),
    )
    assertEquals(
      TabbyTileBroadcastAction.ServiceRecreated,
      tabbyTileBroadcastActionFromString(
        action = "service-recreated",
        clashStartedAction = "clash-started",
        clashStoppedAction = "clash-stopped",
        serviceRecreatedAction = "service-recreated",
        profileLoadedAction = "profile-loaded",
      ),
    )
    assertEquals(
      TabbyTileBroadcastAction.ProfileLoaded,
      tabbyTileBroadcastActionFromString(
        action = "profile-loaded",
        clashStartedAction = "clash-started",
        clashStoppedAction = "clash-stopped",
        serviceRecreatedAction = "service-recreated",
        profileLoadedAction = "profile-loaded",
      ),
    )
  }

  @Test
  fun tabbyTileBroadcastActionFromStringIgnoresUnknownBroadcastActions() {
    assertEquals(
      null,
      tabbyTileBroadcastActionFromString(
        action = "unknown",
        clashStartedAction = "clash-started",
        clashStoppedAction = "clash-stopped",
        serviceRecreatedAction = "service-recreated",
        profileLoadedAction = "profile-loaded",
      ),
    )
    assertEquals(
      null,
      tabbyTileBroadcastActionFromString(
        action = null,
        clashStartedAction = "clash-started",
        clashStoppedAction = "clash-stopped",
        serviceRecreatedAction = "service-recreated",
        profileLoadedAction = "profile-loaded",
      ),
    )
  }

  @Test
  fun tabbyTileInitialStateUsesCurrentProfilePresenceAsRunningState() {
    assertEquals(
      TabbyTileState(),
      tabbyTileInitialState(currentProfile = null),
    )
    assertEquals(
      TabbyTileState(clashRunning = true, currentProfile = "profile-a"),
      tabbyTileInitialState(currentProfile = "profile-a"),
    )
  }

  @Test
  fun reduceTabbyTileStateMarksClashStartedAndClearsProfile() {
    assertEquals(
      TabbyTileState(clashRunning = true, currentProfile = ""),
      reduceTabbyTileState(
        state = TabbyTileState(clashRunning = false, currentProfile = "old-profile"),
        event = TabbyTileEvent.ClashStarted,
      ),
    )
  }

  @Test
  fun reduceTabbyTileStateMarksClashStoppedAndClearsProfile() {
    assertEquals(
      TabbyTileState(clashRunning = false, currentProfile = ""),
      reduceTabbyTileState(
        state = TabbyTileState(clashRunning = true, currentProfile = "old-profile"),
        event = TabbyTileEvent.ClashStopped,
      ),
    )
  }

  @Test
  fun reduceTabbyTileStateMarksServiceRecreatedAsStoppedAndClearsProfile() {
    assertEquals(
      TabbyTileState(clashRunning = false, currentProfile = ""),
      reduceTabbyTileState(
        state = TabbyTileState(clashRunning = true, currentProfile = "old-profile"),
        event = TabbyTileEvent.ServiceRecreated,
      ),
    )
  }

  @Test
  fun reduceTabbyTileStateUpdatesLoadedProfileWithoutChangingRunningState() {
    assertEquals(
      TabbyTileState(clashRunning = true, currentProfile = "profile-b"),
      reduceTabbyTileState(
        state = TabbyTileState(clashRunning = true, currentProfile = ""),
        event = TabbyTileEvent.ProfileLoaded("profile-b"),
      ),
    )
    assertEquals(
      TabbyTileState(clashRunning = false, currentProfile = ""),
      reduceTabbyTileState(
        state = TabbyTileState(clashRunning = false, currentProfile = "old-profile"),
        event = TabbyTileEvent.ProfileLoaded(null),
      ),
    )
  }

  @Test
  fun tabbyTileClickActionStartsClashWhenInactive() {
    assertEquals(
      TabbyTileClickAction.StartClash,
      tabbyTileClickAction(TabbyTileClickState.Inactive),
    )
  }

  @Test
  fun tabbyTileClickActionStopsClashWhenActive() {
    assertEquals(
      TabbyTileClickAction.StopClash,
      tabbyTileClickAction(TabbyTileClickState.Active),
    )
  }

  @Test
  fun tabbyTileClickActionIgnoresOtherStates() {
    assertEquals(
      TabbyTileClickAction.Ignore,
      tabbyTileClickAction(TabbyTileClickState.Other),
    )
  }

  @Test
  fun tabbyTileClickActionIgnoresMissingTileState() {
    assertEquals(
      TabbyTileClickAction.Ignore,
      tabbyTileClickAction(null),
    )
  }

  @Test
  fun tabbyTilePresentationUsesRunningStateAndProfileName() {
    assertEquals(
      TabbyTilePresentation(active = true, profileName = "profile-a"),
      tabbyTilePresentation(TabbyTileState(clashRunning = true, currentProfile = "profile-a")),
    )
  }

  @Test
  fun tabbyTilePresentationUsesDefaultLabelWhenProfileNameIsEmpty() {
    assertEquals(
      TabbyTilePresentation(active = false, profileName = null),
      tabbyTilePresentation(TabbyTileState(clashRunning = false, currentProfile = "")),
    )
  }

  @Test
  fun tabbyTilePresentationLabelUsesProfileNameOrDefaultLabel() {
    assertEquals(
      "profile-a",
      tabbyTilePresentationLabel(
        presentation = TabbyTilePresentation(active = true, profileName = "profile-a"),
        defaultLabel = "Tabby",
      ),
    )
    assertEquals(
      "Tabby",
      tabbyTilePresentationLabel(
        presentation = TabbyTilePresentation(active = false, profileName = null),
        defaultLabel = "Tabby",
      ),
    )
  }

  @Test
  fun tabbyTileBroadcastPlanReducesDirectTileEvents() {
    assertEquals(
      TabbyTileBroadcastPlan.Reduce(TabbyTileEvent.ClashStarted),
      tabbyTileBroadcastPlan(TabbyTileBroadcastAction.ClashStarted),
    )
    assertEquals(
      TabbyTileBroadcastPlan.Reduce(TabbyTileEvent.ClashStopped),
      tabbyTileBroadcastPlan(TabbyTileBroadcastAction.ClashStopped),
    )
    assertEquals(
      TabbyTileBroadcastPlan.Reduce(TabbyTileEvent.ServiceRecreated),
      tabbyTileBroadcastPlan(TabbyTileBroadcastAction.ServiceRecreated),
    )
  }

  @Test
  fun tabbyTileBroadcastPlanRequestsCurrentProfileForProfileLoadedBroadcast() {
    assertEquals(
      TabbyTileBroadcastPlan.LoadCurrentProfile,
      tabbyTileBroadcastPlan(TabbyTileBroadcastAction.ProfileLoaded),
    )
  }

  @Test
  fun tabbyTileBroadcastPlanIgnoresUnknownBroadcasts() {
    assertEquals(
      TabbyTileBroadcastPlan.Ignore,
      tabbyTileBroadcastPlan(null),
    )
  }

  @Test
  fun tabbyTileProfileLoadedEventWrapsCurrentProfile() {
    assertEquals(
      TabbyTileEvent.ProfileLoaded("profile-c"),
      tabbyTileProfileLoadedEvent("profile-c"),
    )
    assertEquals(
      TabbyTileEvent.ProfileLoaded(null),
      tabbyTileProfileLoadedEvent(null),
    )
  }
}
