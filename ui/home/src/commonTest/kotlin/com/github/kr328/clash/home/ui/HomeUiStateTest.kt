package com.github.kr328.clash.home.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class HomeUiStateTest {
  @Test
  fun fetchedStateShowsModeWhenClashIsRunningAndKeepsForwardedTraffic() {
    val state =
      HomeUiState(forwarded = "10 MB")
        .withFetchedHomeState(
          clashRunning = true,
          mode = "Rule",
          hasProviders = true,
          profileName = "Daily",
        )

    assertEquals("10 MB", state.forwarded)
    assertEquals("Rule", state.mode)
    assertEquals(true, state.hasProviders)
    assertEquals("Daily", state.profileName)
  }

  @Test
  fun fetchedStateClearsModeWhenClashIsStoppedAndUpdatesProfileAndProviders() {
    val state =
      HomeUiState(forwarded = "10 MB", mode = "Global", hasProviders = true, profileName = "Old")
        .withFetchedHomeState(
          clashRunning = false,
          mode = "Rule",
          hasProviders = false,
          profileName = "New",
        )

    assertEquals("10 MB", state.forwarded)
    assertEquals(null, state.mode)
    assertEquals(false, state.hasProviders)
    assertEquals("New", state.profileName)
  }

  @Test
  fun forwardedTrafficUpdatePreservesFetchedStateFields() {
    val state =
      HomeUiState(mode = "Direct", hasProviders = true, profileName = "Daily")
        .withForwardedTraffic("42 MB")

    assertEquals("42 MB", state.forwarded)
    assertEquals("Direct", state.mode)
    assertEquals(true, state.hasProviders)
    assertEquals("Daily", state.profileName)
  }
}
