package com.github.kr328.clash.common.remote

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RemoteBroadcastActionTest {
  private val serviceRecreatedAction = "service-recreated"
  private val clashStartedAction = "clash-started"
  private val clashStoppedAction = "clash-stopped"
  private val profileChangedAction = "profile-changed"
  private val profileUpdateCompletedAction = "profile-update-completed"
  private val profileUpdateFailedAction = "profile-update-failed"
  private val profileLoadedAction = "profile-loaded"

  @Test
  fun mapsKnownPlatformActionsToRemoteBroadcastActions() {
    assertEquals(
      TabbyRemoteBroadcastAction.ServiceRecreated,
      tabbyRemoteBroadcastActionFromPlatformAction(
        action = serviceRecreatedAction,
        serviceRecreatedAction = serviceRecreatedAction,
        clashStartedAction = clashStartedAction,
        clashStoppedAction = clashStoppedAction,
        profileChangedAction = profileChangedAction,
        profileUpdateCompletedAction = profileUpdateCompletedAction,
        profileUpdateFailedAction = profileUpdateFailedAction,
        profileLoadedAction = profileLoadedAction,
      ),
    )
    assertEquals(
      TabbyRemoteBroadcastAction.ClashStarted,
      tabbyRemoteBroadcastActionFromPlatformAction(
        action = clashStartedAction,
        serviceRecreatedAction = serviceRecreatedAction,
        clashStartedAction = clashStartedAction,
        clashStoppedAction = clashStoppedAction,
        profileChangedAction = profileChangedAction,
        profileUpdateCompletedAction = profileUpdateCompletedAction,
        profileUpdateFailedAction = profileUpdateFailedAction,
        profileLoadedAction = profileLoadedAction,
      ),
    )
    assertEquals(
      TabbyRemoteBroadcastAction.ClashStopped,
      tabbyRemoteBroadcastActionFromPlatformAction(
        action = clashStoppedAction,
        serviceRecreatedAction = serviceRecreatedAction,
        clashStartedAction = clashStartedAction,
        clashStoppedAction = clashStoppedAction,
        profileChangedAction = profileChangedAction,
        profileUpdateCompletedAction = profileUpdateCompletedAction,
        profileUpdateFailedAction = profileUpdateFailedAction,
        profileLoadedAction = profileLoadedAction,
      ),
    )
    assertEquals(
      TabbyRemoteBroadcastAction.ProfileChanged,
      tabbyRemoteBroadcastActionFromPlatformAction(
        action = profileChangedAction,
        serviceRecreatedAction = serviceRecreatedAction,
        clashStartedAction = clashStartedAction,
        clashStoppedAction = clashStoppedAction,
        profileChangedAction = profileChangedAction,
        profileUpdateCompletedAction = profileUpdateCompletedAction,
        profileUpdateFailedAction = profileUpdateFailedAction,
        profileLoadedAction = profileLoadedAction,
      ),
    )
    assertEquals(
      TabbyRemoteBroadcastAction.ProfileUpdateCompleted,
      tabbyRemoteBroadcastActionFromPlatformAction(
        action = profileUpdateCompletedAction,
        serviceRecreatedAction = serviceRecreatedAction,
        clashStartedAction = clashStartedAction,
        clashStoppedAction = clashStoppedAction,
        profileChangedAction = profileChangedAction,
        profileUpdateCompletedAction = profileUpdateCompletedAction,
        profileUpdateFailedAction = profileUpdateFailedAction,
        profileLoadedAction = profileLoadedAction,
      ),
    )
    assertEquals(
      TabbyRemoteBroadcastAction.ProfileUpdateFailed,
      tabbyRemoteBroadcastActionFromPlatformAction(
        action = profileUpdateFailedAction,
        serviceRecreatedAction = serviceRecreatedAction,
        clashStartedAction = clashStartedAction,
        clashStoppedAction = clashStoppedAction,
        profileChangedAction = profileChangedAction,
        profileUpdateCompletedAction = profileUpdateCompletedAction,
        profileUpdateFailedAction = profileUpdateFailedAction,
        profileLoadedAction = profileLoadedAction,
      ),
    )
    assertEquals(
      TabbyRemoteBroadcastAction.ProfileLoaded,
      tabbyRemoteBroadcastActionFromPlatformAction(
        action = profileLoadedAction,
        serviceRecreatedAction = serviceRecreatedAction,
        clashStartedAction = clashStartedAction,
        clashStoppedAction = clashStoppedAction,
        profileChangedAction = profileChangedAction,
        profileUpdateCompletedAction = profileUpdateCompletedAction,
        profileUpdateFailedAction = profileUpdateFailedAction,
        profileLoadedAction = profileLoadedAction,
      ),
    )
  }

  @Test
  fun ignoresMissingOrUnknownPlatformActions() {
    assertNull(remoteBroadcastAction(action = null))
    assertNull(remoteBroadcastAction(action = "other-action"))
  }

  @Test
  fun mapsClashRunningStateOnlyForLifecycleActions() {
    assertFalse(
      tabbyRemoteBroadcastClashRunningState(TabbyRemoteBroadcastAction.ServiceRecreated)
        ?: error("missing state")
    )
    assertTrue(
      tabbyRemoteBroadcastClashRunningState(TabbyRemoteBroadcastAction.ClashStarted)
        ?: error("missing state")
    )
    assertFalse(
      tabbyRemoteBroadcastClashRunningState(TabbyRemoteBroadcastAction.ClashStopped)
        ?: error("missing state")
    )

    assertNull(tabbyRemoteBroadcastClashRunningState(TabbyRemoteBroadcastAction.ProfileChanged))
    assertNull(
      tabbyRemoteBroadcastClashRunningState(TabbyRemoteBroadcastAction.ProfileUpdateCompleted)
    )
    assertNull(
      tabbyRemoteBroadcastClashRunningState(TabbyRemoteBroadcastAction.ProfileUpdateFailed)
    )
    assertNull(tabbyRemoteBroadcastClashRunningState(TabbyRemoteBroadcastAction.ProfileLoaded))
  }

  private fun remoteBroadcastAction(action: String?): TabbyRemoteBroadcastAction? {
    return tabbyRemoteBroadcastActionFromPlatformAction(
      action = action,
      serviceRecreatedAction = serviceRecreatedAction,
      clashStartedAction = clashStartedAction,
      clashStoppedAction = clashStoppedAction,
      profileChangedAction = profileChangedAction,
      profileUpdateCompletedAction = profileUpdateCompletedAction,
      profileUpdateFailedAction = profileUpdateFailedAction,
      profileLoadedAction = profileLoadedAction,
    )
  }
}
