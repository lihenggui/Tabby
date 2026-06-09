package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile

internal data class ProfileFilesUiState<T>(
  val configFiles: List<T> = emptyList(),
  val currentInBaseDir: Boolean = true,
  val configurationEditable: Boolean = false,
)

internal fun <T> profileFilesInitialUiState(): ProfileFilesUiState<T> {
  return ProfileFilesUiState()
}

internal sealed interface ProfileFilesEventState<out DocumentT, out OpenFileT> {
  data object Idle : ProfileFilesEventState<Nothing, Nothing>

  data object Finish : ProfileFilesEventState<Nothing, Nothing>

  data class OpenFile<out OpenFileT>(val openFile: OpenFileT) :
    ProfileFilesEventState<Nothing, OpenFileT>

  data class RequestImport<out DocumentT>(val targetDocument: DocumentT?) :
    ProfileFilesEventState<DocumentT, Nothing>

  data class RequestExport<out DocumentT>(val sourceDocument: DocumentT) :
    ProfileFilesEventState<DocumentT, Nothing>

  data class ShowMessage(val message: String) : ProfileFilesEventState<Nothing, Nothing>
}

internal sealed interface ProfileFilesEventRouteEffect<out DocumentT, out OpenFileT> {
  data object Ignore : ProfileFilesEventRouteEffect<Nothing, Nothing>

  data object Finish : ProfileFilesEventRouteEffect<Nothing, Nothing>

  data class OpenFile<out OpenFileT>(val openFile: OpenFileT) :
    ProfileFilesEventRouteEffect<Nothing, OpenFileT>

  data class RequestImport<out DocumentT>(val targetDocument: DocumentT?) :
    ProfileFilesEventRouteEffect<DocumentT, Nothing>

  data class RequestExport<out DocumentT>(val sourceDocument: DocumentT) :
    ProfileFilesEventRouteEffect<DocumentT, Nothing>

  data class ShowMessage(val message: String) : ProfileFilesEventRouteEffect<Nothing, Nothing>
}

internal fun <DocumentT, OpenFileT> profileFilesInitialEventState():
  ProfileFilesEventState<DocumentT, OpenFileT> {
  return ProfileFilesEventState.Idle
}

internal fun <DocumentT, OpenFileT> profileFilesEventRouteEffect(
  eventState: ProfileFilesEventState<DocumentT, OpenFileT>
): ProfileFilesEventRouteEffect<DocumentT, OpenFileT> {
  return when (eventState) {
    ProfileFilesEventState.Idle -> ProfileFilesEventRouteEffect.Ignore
    ProfileFilesEventState.Finish -> ProfileFilesEventRouteEffect.Finish
    is ProfileFilesEventState.OpenFile -> ProfileFilesEventRouteEffect.OpenFile(eventState.openFile)
    is ProfileFilesEventState.RequestImport ->
      ProfileFilesEventRouteEffect.RequestImport(eventState.targetDocument)
    is ProfileFilesEventState.RequestExport ->
      ProfileFilesEventRouteEffect.RequestExport(eventState.sourceDocument)
    is ProfileFilesEventState.ShowMessage ->
      ProfileFilesEventRouteEffect.ShowMessage(eventState.message)
  }
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

internal fun <DocumentT> profileFileImportRequestEventState(
  targetDocument: DocumentT?
): ProfileFilesEventState<DocumentT, Nothing> {
  return ProfileFilesEventState.RequestImport(targetDocument)
}

internal fun <DocumentT> profileFileExportRequestEventState(
  sourceDocument: DocumentT
): ProfileFilesEventState<DocumentT, Nothing> {
  return ProfileFilesEventState.RequestExport(sourceDocument)
}
