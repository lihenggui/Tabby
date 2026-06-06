package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class ProfileUpdateRulesTest {
  @Test
  fun marksImportedNonFileProfilesAsUpdatable() {
    assertTrue(isProfileUpdatable(profile(type = Profile.Type.Url, imported = true)))
    assertTrue(isProfileUpdatable(profile(type = Profile.Type.External, imported = true)))
  }

  @Test
  fun excludesFileProfilesAndUnsavedProfilesFromUpdates() {
    assertFalse(isProfileUpdatable(profile(type = Profile.Type.File, imported = true)))
    assertFalse(isProfileUpdatable(profile(type = Profile.Type.Url, imported = false)))
    assertFalse(isProfileUpdatable(profile(type = Profile.Type.External, imported = false)))
  }

  @Test
  fun detectsWhetherAListContainsUpdatableProfiles() {
    assertFalse(
      hasUpdatableProfiles(
        listOf(
          profile(type = Profile.Type.File, imported = true),
          profile(type = Profile.Type.Url, imported = false),
        )
      )
    )
    assertTrue(
      hasUpdatableProfiles(
        listOf(
          profile(type = Profile.Type.File, imported = true),
          profile(type = Profile.Type.Url, imported = true),
        )
      )
    )
  }

  @Test
  fun filtersProfilesThatShouldBeUpdated() {
    val url = profile(type = Profile.Type.Url, imported = true)
    val external = profile(type = Profile.Type.External, imported = true)
    val file = profile(type = Profile.Type.File, imported = true)
    val pendingUrl = profile(type = Profile.Type.Url, imported = false)

    assertEquals(
      listOf(url, external),
      filterUpdatableProfiles(listOf(file, url, pendingUrl, external)),
    )
  }

  private fun profile(type: Profile.Type, imported: Boolean): Profile {
    return Profile(
      uuid = Uuid.parse("00000000-0000-0000-0000-000000000001"),
      name = "Profile",
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
