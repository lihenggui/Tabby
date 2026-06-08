package com.github.kr328.clash.profile.ui

internal sealed interface ProfileFilesFetchAction {
  data class Fetch(val documentId: String, val inBaseDirectory: Boolean) : ProfileFilesFetchAction

  data object Ignore : ProfileFilesFetchAction
}

internal enum class ProfileFilesPlatformStartEvent {
  Start,
  Other,
}

internal fun profileFilesFetchAction(location: ProfileFilesLocation): ProfileFilesFetchAction {
  return if (location.initialized) {
    ProfileFilesFetchAction.Fetch(
      documentId = location.currentDocumentId,
      inBaseDirectory = location.currentInBaseDir,
    )
  } else {
    ProfileFilesFetchAction.Ignore
  }
}

internal fun profileFilesRefreshRequestedFromPlatformStartEvent(
  event: ProfileFilesPlatformStartEvent
): Boolean {
  return when (event) {
    ProfileFilesPlatformStartEvent.Start -> true
    ProfileFilesPlatformStartEvent.Other -> false
  }
}
