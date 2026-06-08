package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.FetchStatus
import com.github.kr328.clash.core.model.Profile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class PropertiesStateMapperTest {
  @Test
  fun initialStatesUseDefaultUiStateAndIdleEvent() {
    assertEquals(PropertiesUiState(), propertiesInitialUiState())
    assertEquals(PropertiesEventState.Idle, propertiesInitialEventState())
  }

  @Test
  fun propertiesEventPlatformActionMapsEventStates() {
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")

    assertEquals(
      PropertiesEventPlatformAction.Ignore,
      propertiesEventPlatformAction(PropertiesEventState.Idle),
    )
    assertEquals(
      PropertiesEventPlatformAction.BrowseFiles(uuid),
      propertiesEventPlatformAction(PropertiesEventState.BrowseFiles(uuid)),
    )
    assertEquals(
      PropertiesEventPlatformAction.Finish(success = true),
      propertiesEventPlatformAction(PropertiesEventState.Finish(success = true)),
    )
    assertEquals(
      PropertiesEventPlatformAction.ShowMessage("commit failed"),
      propertiesEventPlatformAction(PropertiesEventState.ShowMessage("commit failed")),
    )
  }

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
  fun propertiesInitActionLoadsExistingProfileOrFinishesWhenMissing() {
    val profile = profile()

    assertEquals(
      PropertiesInitAction.LoadProfile(profile),
      propertiesInitAction(profile),
    )
    assertEquals(
      PropertiesInitAction.Finish,
      propertiesInitAction(null),
    )
  }

  @Test
  fun propertiesInitEventStateFinishesOnlyWhenProfileIsMissing() {
    val profile = profile()

    assertEquals(
      null,
      propertiesInitEventState(PropertiesInitAction.LoadProfile(profile)),
    )
    assertEquals(
      PropertiesEventState.Finish(success = false),
      propertiesInitEventState(PropertiesInitAction.Finish),
    )
  }

  @Test
  fun propertiesBrowseFilesActionRequiresRootUuid() {
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")

    assertEquals(
      PropertiesBrowseFilesAction.BrowseFiles(uuid),
      propertiesBrowseFilesAction(uuid),
    )
    assertEquals(
      PropertiesBrowseFilesAction.Ignore,
      propertiesBrowseFilesAction(null),
    )
  }

  @Test
  fun propertiesBrowseFilesEventStateBrowsesOnlyForBrowseAction() {
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")

    assertEquals(
      PropertiesEventState.BrowseFiles(uuid),
      propertiesBrowseFilesEventState(PropertiesBrowseFilesAction.BrowseFiles(uuid)),
    )
    assertEquals(
      null,
      propertiesBrowseFilesEventState(PropertiesBrowseFilesAction.Ignore),
    )
  }

  @Test
  fun propertiesCommitValidationRejectsBlankName() {
    assertEquals(
      PropertiesCommitValidationResult.EmptyName,
      validatePropertiesCommit(profile(name = "   ")),
    )
  }

  @Test
  fun propertiesCommitValidationRejectsBlankSourceForRemoteProfiles() {
    assertEquals(
      PropertiesCommitValidationResult.EmptySource,
      validatePropertiesCommit(profile(type = Profile.Type.Url, source = "   ")),
    )
    assertEquals(
      PropertiesCommitValidationResult.EmptySource,
      validatePropertiesCommit(profile(type = Profile.Type.External, source = "")),
    )
  }

  @Test
  fun propertiesCommitValidationAllowsBlankSourceForFileProfiles() {
    assertEquals(
      PropertiesCommitValidationResult.Valid,
      validatePropertiesCommit(profile(type = Profile.Type.File, source = "")),
    )
  }

  @Test
  fun propertiesCommitValidationAcceptsValidRemoteProfile() {
    assertEquals(PropertiesCommitValidationResult.Valid, validatePropertiesCommit(profile()))
  }

  @Test
  fun propertiesCommitActionIgnoresMissingProfile() {
    assertEquals(
      PropertiesCommitAction.Ignore,
      propertiesCommitAction(PropertiesUiState(profile = null)),
    )
  }

  @Test
  fun propertiesCommitActionReportsValidationFailures() {
    assertEquals(
      PropertiesCommitAction.ShowEmptyName,
      propertiesCommitAction(PropertiesUiState(profile = profile(name = "   "))),
    )
    assertEquals(
      PropertiesCommitAction.ShowEmptySource,
      propertiesCommitAction(
        PropertiesUiState(profile = profile(type = Profile.Type.Url, source = "   "))
      ),
    )
  }

  @Test
  fun propertiesCommitActionCommitsValidProfile() {
    val profile = profile()

    assertEquals(
      PropertiesCommitAction.Commit(profile),
      propertiesCommitAction(PropertiesUiState(profile = profile)),
    )
  }

  @Test
  fun propertiesCommitValidationEventStateMapsValidationFailuresToMessages() {
    assertEquals(
      PropertiesEventState.ShowMessage("empty name"),
      propertiesCommitValidationEventState(
        action = PropertiesCommitAction.ShowEmptyName,
        emptyNameMessage = "empty name",
        emptySourceMessage = "empty source",
      ),
    )
    assertEquals(
      PropertiesEventState.ShowMessage("empty source"),
      propertiesCommitValidationEventState(
        action = PropertiesCommitAction.ShowEmptySource,
        emptyNameMessage = "empty name",
        emptySourceMessage = "empty source",
      ),
    )
  }

  @Test
  fun propertiesCommitValidationEventStateIgnoresNonValidationActions() {
    val profile = profile()

    assertEquals(
      null,
      propertiesCommitValidationEventState(
        action = PropertiesCommitAction.Ignore,
        emptyNameMessage = "empty name",
        emptySourceMessage = "empty source",
      ),
    )
    assertEquals(
      null,
      propertiesCommitValidationEventState(
        action = PropertiesCommitAction.Commit(profile),
        emptyNameMessage = "empty name",
        emptySourceMessage = "empty source",
      ),
    )
  }

  @Test
  fun propertiesFinishEventStateCarriesSuccessFlag() {
    assertEquals(PropertiesEventState.Finish(success = true), propertiesFinishEventState(true))
    assertEquals(PropertiesEventState.Finish(success = false), propertiesFinishEventState(false))
  }

  @Test
  fun propertiesErrorEventStateFallsBackToUnknownMessage() {
    assertEquals(
      PropertiesEventState.ShowMessage("commit failed"),
      propertiesErrorEventState("commit failed", "Unknown error"),
    )
    assertEquals(
      PropertiesEventState.ShowMessage("Unknown error"),
      propertiesErrorEventState(null, "Unknown error"),
    )
  }

  @Test
  fun consumedEventStateResetsToIdle() {
    assertEquals(PropertiesEventState.Idle, propertiesConsumedEventState())
  }

  @Test
  fun propertiesAutoSaveActionSavesUnsavedProfileWhenNotCanceled() {
    val profile = profile(name = "Changed")
    val state = PropertiesUiState(profile = profile, hasUnsavedChanges = true)

    assertEquals(
      PropertiesAutoSaveAction.Save(profile),
      propertiesAutoSaveAction(canceled = false, state = state),
    )
  }

  @Test
  fun propertiesAutoSaveActionIgnoresCanceledOrUnchangedOrMissingProfiles() {
    val profile = profile(name = "Changed")

    assertEquals(
      PropertiesAutoSaveAction.Ignore,
      propertiesAutoSaveAction(
        canceled = true,
        state = PropertiesUiState(profile = profile, hasUnsavedChanges = true),
      ),
    )
    assertEquals(
      PropertiesAutoSaveAction.Ignore,
      propertiesAutoSaveAction(
        canceled = false,
        state = PropertiesUiState(profile = profile, hasUnsavedChanges = false),
      ),
    )
    assertEquals(
      PropertiesAutoSaveAction.Ignore,
      propertiesAutoSaveAction(
        canceled = false,
        state = PropertiesUiState(profile = null, hasUnsavedChanges = true),
      ),
    )
  }

  @Test
  fun propertiesAutoSaveRequestedFromPlatformLifecycleEventOnlyRequestsAutoSaveOnStop() {
    assertEquals(
      true,
      propertiesAutoSaveRequestedFromPlatformLifecycleEvent(stopEvent = true),
    )
    assertEquals(
      false,
      propertiesAutoSaveRequestedFromPlatformLifecycleEvent(stopEvent = false),
    )
  }

  @Test
  fun propertiesAutoSaveUiStateTracksProfileAgainstSavedProfile() {
    val saved = profile(name = "Saved")
    val changed = saved.copy(name = "Changed")

    assertEquals(
      PropertiesUiState(
        profile = changed,
        originalProfile = saved,
        hasUnsavedChanges = true,
      ),
      propertiesAutoSaveUiState(changed, saved),
    )
    assertEquals(
      PropertiesUiState(
        profile = saved,
        originalProfile = saved,
        hasUnsavedChanges = false,
      ),
      propertiesAutoSaveUiState(saved, saved),
    )
    assertEquals(
      PropertiesUiState(
        profile = null,
        originalProfile = saved,
        hasUnsavedChanges = false,
      ),
      propertiesAutoSaveUiState(null, saved),
    )
  }

  @Test
  fun propertiesBackActionIgnoresWhileProcessing() {
    assertEquals(
      PropertiesBackAction.Ignore,
      propertiesBackAction(
        processing = true,
        showExitWithoutSavingDialog = true,
        hasUnsavedChanges = true,
      ),
    )
  }

  @Test
  fun propertiesBackActionHidesExistingExitDialog() {
    assertEquals(
      PropertiesBackAction.HideExitWithoutSavingDialog,
      propertiesBackAction(
        processing = false,
        showExitWithoutSavingDialog = true,
        hasUnsavedChanges = true,
      ),
    )
  }

  @Test
  fun propertiesBackActionShowsExitDialogForUnsavedChanges() {
    assertEquals(
      PropertiesBackAction.ShowExitWithoutSavingDialog,
      propertiesBackAction(
        processing = false,
        showExitWithoutSavingDialog = false,
        hasUnsavedChanges = true,
      ),
    )
  }

  @Test
  fun propertiesBackActionRequestsCloseWhenNothingBlocksBack() {
    assertEquals(
      PropertiesBackAction.RequestClose,
      propertiesBackAction(
        processing = false,
        showExitWithoutSavingDialog = false,
        hasUnsavedChanges = false,
      ),
    )
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

  @Test
  fun fetchConfigurationStatusUpdatesProgressWithFormattedSource() {
    val state =
      PropertiesUiState(progress = PropertiesProgressState(visible = true))
        .withFetchStatusProgress(
          status =
            FetchStatus(
              action = FetchStatus.Action.FetchConfiguration,
              args = listOf("https://example.com/config.yaml"),
              progress = 0,
              max = 0,
            ),
          formatFetchConfiguration = { "config:$it" },
          formatFetchProvider = { "provider:$it" },
          verifyingText = "Verifying",
        )

    assertEquals(
      PropertiesProgressState(
        visible = true,
        isIndeterminate = true,
        text = "config:https://example.com/config.yaml",
      ),
      state.progress,
    )
  }

  @Test
  fun fetchProvidersStatusUpdatesProgressWithFormattedProviderAndValues() {
    val state =
      PropertiesUiState(progress = PropertiesProgressState(visible = true))
        .withFetchStatusProgress(
          status =
            FetchStatus(
              action = FetchStatus.Action.FetchProviders,
              args = listOf("rules"),
              progress = 4,
              max = 10,
            ),
          formatFetchConfiguration = { "config:$it" },
          formatFetchProvider = { "provider:$it" },
          verifyingText = "Verifying",
        )

    assertEquals(
      PropertiesProgressState(
        visible = true,
        isIndeterminate = false,
        text = "provider:rules",
        progress = 4,
        max = 10,
      ),
      state.progress,
    )
  }

  @Test
  fun verifyingStatusUpdatesProgressWithProvidedLocalizedText() {
    val state =
      PropertiesUiState(progress = PropertiesProgressState(visible = true))
        .withFetchStatusProgress(
          status =
            FetchStatus(
              action = FetchStatus.Action.Verifying,
              args = emptyList(),
              progress = 2,
              max = 3,
            ),
          formatFetchConfiguration = { "config:$it" },
          formatFetchProvider = { "provider:$it" },
          verifyingText = "Localized verifying",
        )

    assertEquals(
      PropertiesProgressState(
        visible = true,
        isIndeterminate = false,
        text = "Localized verifying",
        progress = 2,
        max = 3,
      ),
      state.progress,
    )
  }

  @Test
  fun fetchStatusProgressUsesEmptyTextArgumentWhenArgsAreMissing() {
    val state =
      PropertiesUiState(progress = PropertiesProgressState(visible = true))
        .withFetchStatusProgress(
          status =
            FetchStatus(
              action = FetchStatus.Action.FetchConfiguration,
              args = emptyList(),
              progress = 0,
              max = 0,
            ),
          formatFetchConfiguration = { "config:$it" },
          formatFetchProvider = { "provider:$it" },
          verifyingText = "Verifying",
        )

    assertEquals("config:", state.progress.text)
  }

  private fun profile(
    name: String = "Profile",
    type: Profile.Type = Profile.Type.Url,
    source: String = "https://example.com/config.yaml",
    interval: Long = 0,
  ): Profile {
    return Profile(
      uuid = Uuid.parse("00000000-0000-0000-0000-000000000001"),
      name = name,
      type = type,
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
