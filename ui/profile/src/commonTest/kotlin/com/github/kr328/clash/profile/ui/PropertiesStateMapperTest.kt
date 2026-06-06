package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class PropertiesStateMapperTest {
  @Test
  fun detectsEditableProfilePropertyChanges() {
    val original = profile(name = "Original", source = "https://example.com/a.yaml", interval = 0)

    assertTrue(hasProfilePropertiesChanges(original.copy(name = "Renamed"), original))
    assertTrue(
      hasProfilePropertiesChanges(original.copy(source = "https://example.com/b.yaml"), original)
    )
    assertTrue(hasProfilePropertiesChanges(original.copy(interval = 15), original))
  }

  @Test
  fun ignoresNonEditableProfilePropertyChanges() {
    val original = profile()

    assertFalse(hasProfilePropertiesChanges(original.copy(active = true), original))
    assertFalse(hasProfilePropertiesChanges(original.copy(download = 100), original))
    assertFalse(hasProfilePropertiesChanges(original.copy(updatedAt = 100), original))
  }

  @Test
  fun reportsNoChangesWhenOriginalProfileIsMissing() {
    assertFalse(hasProfilePropertiesChanges(profile(name = "Changed"), original = null))
  }

  @Test
  fun mapsPropertiesProgressState() {
    assertEquals(
      PropertiesProgressState(
        visible = true,
        isIndeterminate = false,
        text = "Fetching",
        progress = 4,
        max = 10,
      ),
      toPropertiesProgressState(
        visible = true,
        isIndeterminate = false,
        text = "Fetching",
        progress = 4,
        max = 10,
      ),
    )
  }

  private fun profile(
    name: String = "Profile",
    source: String = "https://example.com/config.yaml",
    interval: Long = 0,
  ): Profile {
    return Profile(
      uuid = Uuid.parse("00000000-0000-0000-0000-000000000001"),
      name = name,
      type = Profile.Type.Url,
      source = source,
      active = false,
      interval = interval,
      upload = 0,
      download = 0,
      total = 0,
      expire = 0,
      updatedAt = 0,
      imported = true,
      pending = false,
    )
  }
}
