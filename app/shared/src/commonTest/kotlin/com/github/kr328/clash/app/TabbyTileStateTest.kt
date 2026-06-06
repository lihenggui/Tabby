package com.github.kr328.clash.app

import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyTileStateTest {
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
}
