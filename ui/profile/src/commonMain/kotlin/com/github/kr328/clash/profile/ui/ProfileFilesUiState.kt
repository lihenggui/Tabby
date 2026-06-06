package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile

internal data class ProfileFilesUiState<T>(
  val configFiles: List<T> = emptyList(),
  val currentInBaseDir: Boolean = true,
  val configurationEditable: Boolean = false,
)

internal sealed interface ProfileFilesEventState<out ConfigFileT, out OpenFileT> {
  data object Idle : ProfileFilesEventState<Nothing, Nothing>

  data object Finish : ProfileFilesEventState<Nothing, Nothing>

  data class OpenFile<out OpenFileT>(val uri: OpenFileT) :
    ProfileFilesEventState<Nothing, OpenFileT>

  data class RequestImport<out ConfigFileT>(val targetConfigFile: ConfigFileT?) :
    ProfileFilesEventState<ConfigFileT, Nothing>

  data class RequestExport<out ConfigFileT>(val sourceConfigFile: ConfigFileT) :
    ProfileFilesEventState<ConfigFileT, Nothing>

  data class ShowMessage(val message: String) : ProfileFilesEventState<Nothing, Nothing>
}

internal fun isProfileConfigurationEditable(profile: Profile): Boolean {
  return profile.type == Profile.Type.Url
}

internal fun <T> ProfileFilesUiState<T>.withConfigurationEditable(
  configurationEditable: Boolean
): ProfileFilesUiState<T> {
  return copy(configurationEditable = configurationEditable)
}

internal fun <T> ProfileFilesUiState<T>.withConfigFiles(
  configFiles: List<T>,
  currentInBaseDir: Boolean,
): ProfileFilesUiState<T> {
  return copy(configFiles = configFiles, currentInBaseDir = currentInBaseDir)
}

internal fun profileFilesErrorEventState(
  message: String?,
  unknownMessage: String,
): ProfileFilesEventState<Nothing, Nothing> {
  return ProfileFilesEventState.ShowMessage(message ?: unknownMessage)
}

internal fun profileFilesConsumedEventState(): ProfileFilesEventState<Nothing, Nothing> {
  return ProfileFilesEventState.Idle
}

internal fun profileFilesLoadedEventState(
  action: ProfileFilesLoadedAction
): ProfileFilesEventState<Nothing, Nothing>? {
  return when (action) {
    is ProfileFilesLoadedAction.LoadFiles -> null
    ProfileFilesLoadedAction.Finish -> ProfileFilesEventState.Finish
  }
}

internal fun profileFilesBackEventState(
  action: ProfileFilesBackAction
): ProfileFilesEventState<Nothing, Nothing>? {
  return when (action) {
    is ProfileFilesBackAction.LeaveDirectory -> null
    ProfileFilesBackAction.Finish -> ProfileFilesEventState.Finish
  }
}

internal fun <OpenFileT> profileFileOpenEventState(
  action: ProfileFileOpenAction,
  openFile: OpenFileT,
): ProfileFilesEventState<Nothing, OpenFileT>? {
  return when (action) {
    is ProfileFileOpenAction.EnterDirectory -> null
    is ProfileFileOpenAction.OpenFile -> ProfileFilesEventState.OpenFile(openFile)
  }
}

internal fun <ConfigFileT> profileFileImportRequestEventState(
  targetConfigFile: ConfigFileT?
): ProfileFilesEventState<ConfigFileT, Nothing> {
  return ProfileFilesEventState.RequestImport(targetConfigFile)
}

internal fun <ConfigFileT> profileFileExportRequestEventState(
  sourceConfigFile: ConfigFileT
): ProfileFilesEventState<ConfigFileT, Nothing> {
  return ProfileFilesEventState.RequestExport(sourceConfigFile)
}
