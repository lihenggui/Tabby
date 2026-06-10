package com.github.kr328.clash.core.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class ProfileConfigurationLoadTest {
  @Test
  fun profileConfigurationLoadRunsForInitialLoadOrOverrideReload() {
    assertTrue(
      profileShouldLoadConfiguration(
        currentProfile = PROFILE_UUID,
        loadedProfile = null,
        changedProfile = null,
      )
    )
    assertTrue(
      profileShouldLoadConfiguration(
        currentProfile = PROFILE_UUID,
        loadedProfile = PROFILE_UUID,
        changedProfile = null,
      )
    )
  }

  @Test
  fun profileConfigurationLoadRunsWhenActiveProfileChanges() {
    assertTrue(
      profileShouldLoadConfiguration(
        currentProfile = OTHER_PROFILE_UUID,
        loadedProfile = PROFILE_UUID,
        changedProfile = PROFILE_UUID,
      )
    )
  }

  @Test
  fun profileConfigurationLoadRunsWhenLoadedProfileChanged() {
    assertTrue(
      profileShouldLoadConfiguration(
        currentProfile = PROFILE_UUID,
        loadedProfile = PROFILE_UUID,
        changedProfile = PROFILE_UUID,
      )
    )
  }

  @Test
  fun profileConfigurationLoadSkipsUnrelatedProfileChangesWhenCurrentProfileAlreadyLoaded() {
    assertFalse(
      profileShouldLoadConfiguration(
        currentProfile = PROFILE_UUID,
        loadedProfile = PROFILE_UUID,
        changedProfile = OTHER_PROFILE_UUID,
      )
    )
  }

  private companion object {
    val PROFILE_UUID: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")
    val OTHER_PROFILE_UUID: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000002")
  }
}
