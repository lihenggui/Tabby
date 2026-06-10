package com.github.kr328.clash.common.service

import kotlin.test.Test
import kotlin.test.assertEquals

class StatusProviderCallActionTest {
  @Test
  fun delegatesUnknownMethodToPlatformProvider() {
    assertEquals(
      TabbyStatusProviderCallAction.Delegate,
      tabbyStatusProviderCallAction(
        method = "unknown",
        currentProfileMethod = CURRENT_PROFILE_METHOD,
        serviceRunning = true,
        currentProfileName = "Demo",
      ),
    )
  }

  @Test
  fun returnsNoResultForCurrentProfileMethodWhenServiceIsStopped() {
    assertEquals(
      TabbyStatusProviderCallAction.NoResult,
      tabbyStatusProviderCallAction(
        method = CURRENT_PROFILE_METHOD,
        currentProfileMethod = CURRENT_PROFILE_METHOD,
        serviceRunning = false,
        currentProfileName = "Demo",
      ),
    )
  }

  @Test
  fun returnsCurrentProfileNameWhenServiceIsRunning() {
    assertEquals(
      TabbyStatusProviderCallAction.CurrentProfile(name = "Demo"),
      tabbyStatusProviderCallAction(
        method = CURRENT_PROFILE_METHOD,
        currentProfileMethod = CURRENT_PROFILE_METHOD,
        serviceRunning = true,
        currentProfileName = "Demo",
      ),
    )
  }

  @Test
  fun preservesNullCurrentProfileNameWhenServiceIsRunning() {
    assertEquals(
      TabbyStatusProviderCallAction.CurrentProfile(name = null),
      tabbyStatusProviderCallAction(
        method = CURRENT_PROFILE_METHOD,
        currentProfileMethod = CURRENT_PROFILE_METHOD,
        serviceRunning = true,
        currentProfileName = null,
      ),
    )
  }
}

private const val CURRENT_PROFILE_METHOD = "currentProfile"
