package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class ProfilesUiStateTest {
  @Test
  fun initialStatesUseProvidedCurrentTimeAndIdleEvent() {
    assertEquals(ProfilesUiState(currentTime = 123), profilesInitialUiState(currentTime = 123))
    assertEquals(ProfilesEventState.Idle, profilesInitialEventState())
  }

  @Test
  fun profileListUpdateRefreshesUpdatableFlagAndPreservesOtherFields() {
    val updatable = profile(type = Profile.Type.Url, imported = true)
    val state =
      ProfilesUiState(allUpdating = true, currentTime = 123)
        .withProfiles(
          listOf(
            profile(type = Profile.Type.File, imported = true),
            updatable,
          )
        )

    assertEquals(
      listOf(profile(type = Profile.Type.File, imported = true), updatable),
      state.profiles,
    )
    assertEquals(true, state.hasUpdatableProfile)
    assertEquals(true, state.allUpdating)
    assertEquals(123, state.currentTime)
  }

  @Test
  fun profileListUpdateClearsUpdatableFlagWhenNoProfileCanUpdate() {
    val state =
      ProfilesUiState(hasUpdatableProfile = true)
        .withProfiles(
          listOf(
            profile(type = Profile.Type.File, imported = true),
            profile(type = Profile.Type.Url, imported = false),
          )
        )

    assertEquals(false, state.hasUpdatableProfile)
  }

  @Test
  fun updatingAndCurrentTimeUpdatesPreserveProfileList() {
    val profiles = listOf(profile(type = Profile.Type.External, imported = true))
    val state =
      ProfilesUiState(profiles = profiles, hasUpdatableProfile = true)
        .withAllUpdating(true)
        .withCurrentTime(456)

    assertEquals(profiles, state.profiles)
    assertEquals(true, state.hasUpdatableProfile)
    assertEquals(true, state.allUpdating)
    assertEquals(456, state.currentTime)
  }

  private fun profile(type: Profile.Type, imported: Boolean): Profile {
    return Profile(
      uuid = Uuid.parse("00000000-0000-0000-0000-000000000001"),
      name = "${type.name} $imported",
      type = type,
      source = "",
      active = false,
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
