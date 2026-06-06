package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class ProfileFilesUiStateTest {
  @Test
  fun onlyUrlProfileConfigurationIsEditable() {
    assertTrue(isProfileConfigurationEditable(profile(type = Profile.Type.Url)))
    assertFalse(isProfileConfigurationEditable(profile(type = Profile.Type.File)))
    assertFalse(isProfileConfigurationEditable(profile(type = Profile.Type.External)))
  }

  @Test
  fun configurationEditableUpdatePreservesFileState() {
    val files = listOf(testFile("config.yaml"))
    val state =
      ProfileFilesUiState(configFiles = files, currentInBaseDir = false)
        .withConfigurationEditable(true)

    assertEquals(files, state.configFiles)
    assertFalse(state.currentInBaseDir)
    assertTrue(state.configurationEditable)
  }

  @Test
  fun configFilesUpdatePreservesConfigurationEditable() {
    val files = listOf(testFile("config.yaml"), testFile("providers"))
    val state =
      ProfileFilesUiState<TestFile>(configurationEditable = true)
        .withConfigFiles(configFiles = files, currentInBaseDir = false)

    assertEquals(files, state.configFiles)
    assertFalse(state.currentInBaseDir)
    assertTrue(state.configurationEditable)
  }

  @Test
  fun configFilesCanReturnToBaseDirectory() {
    val state =
      ProfileFilesUiState(configFiles = listOf(testFile("rules")), currentInBaseDir = false)
        .withConfigFiles(configFiles = listOf(testFile("config.yaml")), currentInBaseDir = true)

    assertEquals(listOf(testFile("config.yaml")), state.configFiles)
    assertTrue(state.currentInBaseDir)
  }

  private fun testFile(name: String): TestFile {
    return TestFile(name)
  }

  private fun profile(type: Profile.Type): Profile {
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
      imported = true,
      pending = false,
    )
  }

  private data class TestFile(val name: String)
}
