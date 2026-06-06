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

  @Test
  fun loadedAndSavedProfilesResetUnsavedChanges() {
    val profile = profile(name = "Original")
    val changed =
      PropertiesUiState()
        .withLoadedProfile(profile)
        .withProfileName("Changed")
        .withSavedProfile(profile.copy(name = "Changed"))

    assertEquals(profile.copy(name = "Changed"), changed.profile)
    assertEquals(profile.copy(name = "Changed"), changed.originalProfile)
    assertFalse(changed.hasUnsavedChanges)
  }

  @Test
  fun editableProfileChangesUpdateProfileAndUnsavedFlag() {
    val original = profile(name = "Original", source = "https://example.com/a.yaml", interval = 0)
    val state =
      PropertiesUiState()
        .withLoadedProfile(original)
        .withProfileName("Renamed")
        .withProfileSource("https://example.com/b.yaml")
        .withProfileInterval(15)

    assertEquals("Renamed", state.profile?.name)
    assertEquals("https://example.com/b.yaml", state.profile?.source)
    assertEquals(15, state.profile?.interval)
    assertTrue(state.hasUnsavedChanges)
  }

  @Test
  fun editableProfileChangesAreNoOpsWhenProfileIsMissing() {
    val state =
      PropertiesUiState()
        .withProfileName("Renamed")
        .withProfileSource("https://example.com/b.yaml")
        .withProfileInterval(15)

    assertEquals(PropertiesUiState(), state)
  }

  @Test
  fun processingStateUsesLocalizedTextProvidedByAndroidBoundary() {
    val started = PropertiesUiState().withProcessingStarted("Initializing")
    val finished = started.withProcessingFinished()

    assertTrue(started.processing)
    assertEquals(
      PropertiesProgressState(
        visible = true,
        isIndeterminate = true,
        text = "Initializing",
      ),
      started.progress,
    )
    assertFalse(finished.processing)
    assertEquals(false, finished.progress.visible)
    assertEquals(null, finished.progress.text)
  }

  @Test
  fun progressStateReducersPreserveLocalizedTextAndProgressValues() {
    val initial = PropertiesProgressState(visible = true)

    assertEquals(
      PropertiesProgressState(visible = true, isIndeterminate = true, text = "Fetching config"),
      initial.withFetchConfigurationProgress("Fetching config"),
    )
    assertEquals(
      PropertiesProgressState(
        visible = true,
        isIndeterminate = false,
        text = "Fetching provider",
        progress = 4,
        max = 10,
      ),
      initial.withFetchProvidersProgress(text = "Fetching provider", progress = 4, max = 10),
    )
    assertEquals(
      PropertiesProgressState(
        visible = true,
        isIndeterminate = false,
        text = "Verifying",
        progress = 2,
        max = 3,
      ),
      initial.withVerifyingProgress(text = "Verifying", progress = 2, max = 3),
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
