package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.FetchStatus
import com.github.kr328.clash.core.model.Profile

internal data class PropertiesUiState(
  val profile: Profile? = null,
  val originalProfile: Profile? = null,
  val processing: Boolean = false,
  val progress: PropertiesProgressState = PropertiesProgressState(),
  val hasUnsavedChanges: Boolean = false,
)

internal enum class PropertiesCommitValidationResult {
  Valid,
  EmptyName,
  EmptySource,
}

internal enum class PropertiesBackAction {
  Ignore,
  HideExitWithoutSavingDialog,
  ShowExitWithoutSavingDialog,
  RequestClose,
}

internal sealed interface PropertiesAutoSaveAction {
  data class Save(val profile: Profile) : PropertiesAutoSaveAction

  data object Ignore : PropertiesAutoSaveAction
}

internal fun hasProfilePropertiesChanges(profile: Profile, original: Profile?): Boolean {
  if (original == null) return false

  return profile.name != original.name ||
    profile.source != original.source ||
    profile.interval != original.interval
}

internal fun validatePropertiesCommit(profile: Profile): PropertiesCommitValidationResult {
  if (profile.name.isBlank()) return PropertiesCommitValidationResult.EmptyName
  if (profile.type != Profile.Type.File && profile.source.isBlank()) {
    return PropertiesCommitValidationResult.EmptySource
  }

  return PropertiesCommitValidationResult.Valid
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
