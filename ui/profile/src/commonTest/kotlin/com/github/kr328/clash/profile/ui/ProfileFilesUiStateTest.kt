package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class ProfileFilesUiStateTest {
  @Test
  fun initialStatesUseDefaultUiStateAndIdleEvent() {
    val state: ProfileFilesUiState<TestFile> = profileFilesInitialUiState()
    val event: ProfileFilesEventState<TestFile, String> = profileFilesInitialEventState()

    assertEquals(ProfileFilesUiState(), state)
    assertEquals(ProfileFilesEventState.Idle, event)
  }

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

  @Test
  fun eventStateCarriesPlatformPayloadsAsGenericValues() {
    val targetFile = testFile("config.yaml")
    val filePayload = "profile/config.yaml"
    val openFileEvent: ProfileFilesEventState<TestFile, String> =
      ProfileFilesEventState.OpenFile(filePayload)
    val importEvent: ProfileFilesEventState<TestFile, String> =
      ProfileFilesEventState.RequestImport(targetFile)
    val exportEvent: ProfileFilesEventState<TestFile, String> =
      ProfileFilesEventState.RequestExport(targetFile)

    assertEquals(ProfileFilesEventState.OpenFile(filePayload), openFileEvent)
    assertEquals(ProfileFilesEventState.RequestImport(targetFile), importEvent)
    assertEquals(ProfileFilesEventState.RequestExport(targetFile), exportEvent)
  }

  @Test
  fun eventPlatformActionMapsEventStates() {
    val targetFile = testFile("config.yaml")
    val filePayload = "profile/config.yaml"

    assertEquals(
      ProfileFilesEventPlatformAction.Ignore,
      profileFilesEventPlatformAction(ProfileFilesEventState.Idle),
    )
    assertEquals(
      ProfileFilesEventPlatformAction.Finish,
      profileFilesEventPlatformAction(ProfileFilesEventState.Finish),
    )
    assertEquals(
      ProfileFilesEventPlatformAction.OpenFile(filePayload),
      profileFilesEventPlatformAction(ProfileFilesEventState.OpenFile(filePayload)),
    )
    assertEquals(
      ProfileFilesEventPlatformAction.RequestImport(targetFile),
      profileFilesEventPlatformAction(ProfileFilesEventState.RequestImport(targetFile)),
    )
    assertEquals(
      ProfileFilesEventPlatformAction.RequestImport(null),
      profileFilesEventPlatformAction(ProfileFilesEventState.RequestImport(null)),
    )
    assertEquals(
      ProfileFilesEventPlatformAction.RequestExport(targetFile),
      profileFilesEventPlatformAction(ProfileFilesEventState.RequestExport(targetFile)),
    )
    assertEquals(
      ProfileFilesEventPlatformAction.ShowMessage("Import failed"),
      profileFilesEventPlatformAction(ProfileFilesEventState.ShowMessage("Import failed")),
    )
  }

  @Test
  fun errorEventStateFallsBackToUnknownMessage() {
    assertEquals(
      ProfileFilesEventState.ShowMessage("delete failed"),
      profileFilesErrorEventState("delete failed", "Unknown error"),
    )
    assertEquals(
      ProfileFilesEventState.ShowMessage("Unknown error"),
      profileFilesErrorEventState(null, "Unknown error"),
    )
  }

  @Test
  fun consumedEventStateResetsToIdle() {
    val event: ProfileFilesEventState<TestFile, String> = profileFilesConsumedEventState()

    assertEquals(ProfileFilesEventState.Idle, event)
  }

  @Test
  fun loadedEventStateFinishesOnlyWhenProfileIsMissing() {
    assertEquals(
      null,
      profileFilesLoadedEventState(
        ProfileFilesLoadedAction.LoadFiles(configurationEditable = true)
      ),
    )
    assertEquals(
      ProfileFilesEventState.Finish,
      profileFilesLoadedEventState(ProfileFilesLoadedAction.Finish),
    )
  }

  @Test
  fun backEventStateFinishesOnlyWhenLeavingTheRootDirectory() {
    assertEquals(
      null,
      profileFilesBackEventState(
        ProfileFilesBackAction.LeaveDirectory(ProfileFilesLocation().initialize("root"))
      ),
    )
    assertEquals(
      ProfileFilesEventState.Finish,
      profileFilesBackEventState(ProfileFilesBackAction.Finish),
    )
  }

  @Test
  fun openEventStateOpensFilesOnlyForOpenFileActions() {
    val openFile = "content://profile/config.yaml"

    assertEquals(
      null,
      profileFileOpenEventState(
        action = ProfileFileOpenAction.EnterDirectory("providers"),
        openFile = openFile,
      ),
    )
    assertEquals(
      ProfileFilesEventState.OpenFile(openFile),
      profileFileOpenEventState(
        action = ProfileFileOpenAction.OpenFile("config.yaml"),
        openFile = openFile,
      ),
    )
  }

  @Test
  fun importAndExportRequestEventStatesCarryGenericPayloads() {
    val targetFile = testFile("config.yaml")

    assertEquals(
      ProfileFilesEventState.RequestImport(targetFile),
      profileFileImportRequestEventState(targetFile),
    )
    assertEquals(
      ProfileFilesEventState.RequestImport(null),
      profileFileImportRequestEventState<TestFile>(null),
    )
    assertEquals(
      ProfileFilesEventState.RequestExport(targetFile),
      profileFileExportRequestEventState(targetFile),
    )
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
