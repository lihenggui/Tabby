package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.FetchStatus
import com.github.kr328.clash.core.model.Profile
import kotlin.uuid.Uuid

internal data class PropertiesUiState(
  val profile: Profile? = null,
  val originalProfile: Profile? = null,
  val processing: Boolean = false,
  val progress: PropertiesProgressState = PropertiesProgressState(),
  val hasUnsavedChanges: Boolean = false,
)

internal fun propertiesInitialUiState(): PropertiesUiState {
  return PropertiesUiState()
}

internal enum class PropertiesCommitValidationResult {
  Valid,
  EmptyName,
  EmptySource,
}

internal sealed interface PropertiesInitAction {
  data class LoadProfile(val profile: Profile) : PropertiesInitAction

  data object Finish : PropertiesInitAction
}

internal enum class PropertiesBackAction {
  Ignore,
  HideExitWithoutSavingDialog,
  ShowExitWithoutSavingDialog,
  RequestClose,
}

internal sealed interface PropertiesBrowseFilesAction {
  data class BrowseFiles(val uuid: Uuid) : PropertiesBrowseFilesAction

  data object Ignore : PropertiesBrowseFilesAction
}

internal sealed interface PropertiesCommitAction {
  data class Commit(val profile: Profile) : PropertiesCommitAction

  data object ShowEmptyName : PropertiesCommitAction

  data object ShowEmptySource : PropertiesCommitAction

  data object Ignore : PropertiesCommitAction
}

internal sealed interface PropertiesAutoSaveAction {
  data class Save(val profile: Profile) : PropertiesAutoSaveAction

  data object Ignore : PropertiesAutoSaveAction
}

internal sealed interface PropertiesEventState {
  data object Idle : PropertiesEventState

  data class Finish(val success: Boolean) : PropertiesEventState

  data class BrowseFiles(val uuid: Uuid) : PropertiesEventState

  data class ShowMessage(val message: String) : PropertiesEventState
}

internal sealed interface PropertiesEventPlatformAction {
  data object Ignore : PropertiesEventPlatformAction

  data class BrowseFiles(val uuid: Uuid) : PropertiesEventPlatformAction

  data class Finish(val success: Boolean) : PropertiesEventPlatformAction

  data class ShowMessage(val message: String) : PropertiesEventPlatformAction
}

internal fun propertiesInitialEventState(): PropertiesEventState {
  return PropertiesEventState.Idle
}

internal fun propertiesEventPlatformAction(
  eventState: PropertiesEventState
): PropertiesEventPlatformAction {
  return when (eventState) {
    PropertiesEventState.Idle -> PropertiesEventPlatformAction.Ignore
    is PropertiesEventState.BrowseFiles ->
      PropertiesEventPlatformAction.BrowseFiles(eventState.uuid)
    is PropertiesEventState.Finish -> PropertiesEventPlatformAction.Finish(eventState.success)
    is PropertiesEventState.ShowMessage ->
      PropertiesEventPlatformAction.ShowMessage(eventState.message)
  }
}

internal fun hasProfilePropertiesChanges(profile: Profile, original: Profile?): Boolean {
  if (original == null) return false

  return profile.name != original.name ||
    profile.source != original.source ||
    profile.interval != original.interval
}

internal fun propertiesInitAction(profile: Profile?): PropertiesInitAction {
  return profile?.let(PropertiesInitAction::LoadProfile) ?: PropertiesInitAction.Finish
}

internal fun propertiesInitEventState(action: PropertiesInitAction): PropertiesEventState? {
  return when (action) {
    is PropertiesInitAction.LoadProfile -> null
    PropertiesInitAction.Finish -> PropertiesEventState.Finish(success = false)
  }
}

internal fun propertiesBrowseFilesAction(rootUuid: Uuid?): PropertiesBrowseFilesAction {
  return rootUuid?.let(PropertiesBrowseFilesAction::BrowseFiles)
    ?: PropertiesBrowseFilesAction.Ignore
}

internal fun propertiesBrowseFilesEventState(
  action: PropertiesBrowseFilesAction
): PropertiesEventState? {
  return when (action) {
    is PropertiesBrowseFilesAction.BrowseFiles -> PropertiesEventState.BrowseFiles(action.uuid)
    PropertiesBrowseFilesAction.Ignore -> null
  }
}

internal fun validatePropertiesCommit(profile: Profile): PropertiesCommitValidationResult {
  if (profile.name.isBlank()) return PropertiesCommitValidationResult.EmptyName
  if (profile.type != Profile.Type.File && profile.source.isBlank()) {
    return PropertiesCommitValidationResult.EmptySource
  }

  return PropertiesCommitValidationResult.Valid
}

internal fun propertiesCommitAction(state: PropertiesUiState): PropertiesCommitAction {
  val profile = state.profile ?: return PropertiesCommitAction.Ignore

  return when (validatePropertiesCommit(profile)) {
    PropertiesCommitValidationResult.Valid -> PropertiesCommitAction.Commit(profile)
    PropertiesCommitValidationResult.EmptyName -> PropertiesCommitAction.ShowEmptyName
    PropertiesCommitValidationResult.EmptySource -> PropertiesCommitAction.ShowEmptySource
  }
}

internal fun propertiesCommitValidationEventState(
  action: PropertiesCommitAction,
  emptyNameMessage: String,
  emptySourceMessage: String,
): PropertiesEventState? {
  return when (action) {
    PropertiesCommitAction.ShowEmptyName -> PropertiesEventState.ShowMessage(emptyNameMessage)
    PropertiesCommitAction.ShowEmptySource -> PropertiesEventState.ShowMessage(emptySourceMessage)
    is PropertiesCommitAction.Commit,
    PropertiesCommitAction.Ignore -> null
  }
}

internal fun propertiesFinishEventState(success: Boolean): PropertiesEventState {
  return PropertiesEventState.Finish(success)
}

internal fun propertiesErrorEventState(
  message: String?,
  unknownMessage: String,
): PropertiesEventState {
  return PropertiesEventState.ShowMessage(message ?: unknownMessage)
}

internal fun propertiesConsumedEventState(): PropertiesEventState {
  return PropertiesEventState.Idle
}

internal fun propertiesAutoSaveAction(
  canceled: Boolean,
  state: PropertiesUiState,
): PropertiesAutoSaveAction {
  val profile = state.profile
  return if (!canceled && state.hasUnsavedChanges && profile != null) {
    PropertiesAutoSaveAction.Save(profile)
  } else {
    PropertiesAutoSaveAction.Ignore
  }
}

internal fun propertiesAutoSaveRequestedFromStopEvent(isStopEvent: Boolean): Boolean {
  return isStopEvent
}

internal fun propertiesAutoSaveUiState(
  profile: Profile?,
  savedProfile: Profile?,
): PropertiesUiState {
  return PropertiesUiState(
    profile = profile,
    originalProfile = savedProfile,
    hasUnsavedChanges =
      if (profile != null) {
        hasProfilePropertiesChanges(profile, savedProfile)
      } else {
        false
      },
  )
}

internal fun propertiesBackAction(
  processing: Boolean,
  showExitWithoutSavingDialog: Boolean,
  hasUnsavedChanges: Boolean,
): PropertiesBackAction {
  return when {
    processing -> PropertiesBackAction.Ignore
    showExitWithoutSavingDialog -> PropertiesBackAction.HideExitWithoutSavingDialog
    hasUnsavedChanges -> PropertiesBackAction.ShowExitWithoutSavingDialog
    else -> PropertiesBackAction.RequestClose
  }
}

internal fun PropertiesUiState.withLoadedProfile(profile: Profile): PropertiesUiState {
  return copy(profile = profile, originalProfile = profile.copy(), hasUnsavedChanges = false)
}

internal fun PropertiesUiState.withSavedProfile(profile: Profile): PropertiesUiState {
  return copy(originalProfile = profile.copy(), hasUnsavedChanges = false)
}

internal fun PropertiesUiState.withProfileName(name: String): PropertiesUiState {
  val updated = profile?.copy(name = name) ?: return this
  return copy(
    profile = updated,
    hasUnsavedChanges = hasProfilePropertiesChanges(updated, originalProfile),
  )
}

internal fun PropertiesUiState.withProfileSource(source: String): PropertiesUiState {
  val updated = profile?.copy(source = source) ?: return this
  return copy(
    profile = updated,
    hasUnsavedChanges = hasProfilePropertiesChanges(updated, originalProfile),
  )
}

internal fun PropertiesUiState.withProfileInterval(interval: Long): PropertiesUiState {
  val updated = profile?.copy(interval = interval) ?: return this
  return copy(
    profile = updated,
    hasUnsavedChanges = hasProfilePropertiesChanges(updated, originalProfile),
  )
}

internal fun PropertiesUiState.withProcessingStarted(initialText: String): PropertiesUiState {
  return copy(
    processing = true,
    progress =
      PropertiesProgressState(
        visible = true,
        isIndeterminate = true,
        text = initialText,
        progress = 0,
        max = 0,
      ),
  )
}

internal fun PropertiesUiState.withProcessingFinished(): PropertiesUiState {
  return copy(processing = false, progress = progress.copy(visible = false, text = null))
}

internal fun PropertiesUiState.withProgress(progress: PropertiesProgressState): PropertiesUiState {
  return copy(progress = progress)
}

internal fun PropertiesProgressState.withFetchConfigurationProgress(
  text: String
): PropertiesProgressState {
  return copy(text = text, isIndeterminate = true)
}

internal fun PropertiesProgressState.withFetchProvidersProgress(
  text: String,
  max: Int,
  progress: Int,
): PropertiesProgressState {
  return copy(text = text, isIndeterminate = false, max = max, progress = progress)
}

internal fun PropertiesProgressState.withVerifyingProgress(
  text: String,
  max: Int,
  progress: Int,
): PropertiesProgressState {
  return copy(text = text, isIndeterminate = false, max = max, progress = progress)
}

internal fun PropertiesUiState.withFetchStatusProgress(
  status: FetchStatus,
  formatFetchConfiguration: (String) -> String,
  formatFetchProvider: (String) -> String,
  verifyingText: String,
): PropertiesUiState {
  val newProgress =
    when (status.action) {
      FetchStatus.Action.FetchConfiguration ->
        progress.withFetchConfigurationProgress(
          text = formatFetchConfiguration(status.args.getOrNull(0).orEmpty())
        )
      FetchStatus.Action.FetchProviders ->
        progress.withFetchProvidersProgress(
          text = formatFetchProvider(status.args.getOrNull(0).orEmpty()),
          max = status.max,
          progress = status.progress,
        )
      FetchStatus.Action.Verifying ->
        progress.withVerifyingProgress(
          text = verifyingText,
          max = status.max,
          progress = status.progress,
        )
    }

  return withProgress(newProgress)
}

internal fun toPropertiesProgressState(
  visible: Boolean,
  isIndeterminate: Boolean,
  text: String?,
  progress: Int,
  max: Int,
): PropertiesProgressState {
  return PropertiesProgressState(
    visible = visible,
    isIndeterminate = isIndeterminate,
    text = text,
    progress = progress,
    max = max,
  )
}
