package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProfileFilesUiStateTest {
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

  private data class TestFile(val name: String)
}
