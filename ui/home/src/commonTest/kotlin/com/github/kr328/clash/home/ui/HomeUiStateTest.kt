package com.github.kr328.clash.home.ui

import com.github.kr328.clash.core.model.Profile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class HomeUiStateTest {
  @Test
  fun homeStartActionStartsEngineForImportedActiveProfile() {
    assertEquals(
      HomeStartAction.StartEngine,
      homeStartAction(profile(imported = true)),
    )
  }

  @Test
  fun homeStartActionShowsNoProfileMessageWhenActiveProfileIsMissingOrNotImported() {
    assertEquals(
      HomeStartAction.ShowNoProfileMessage,
      homeStartAction(null),
    )
    assertEquals(
      HomeStartAction.ShowNoProfileMessage,
      homeStartAction(profile(imported = false)),
    )
  }

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

  private fun profile(imported: Boolean): Profile {
    return Profile(
      uuid = Uuid.parse("00000000-0000-0000-0000-000000000001"),
      name = "Profile",
      type = Profile.Type.Url,
      source = "https://example.com/config.yaml",
      active = true,
      interval = 0,
      upload = 0,
      download = 0,
      total = 0,
      expire = 0,
      updatedAt = 0,
      imported = imported,
      pending = !imported,
    )
  }
}
