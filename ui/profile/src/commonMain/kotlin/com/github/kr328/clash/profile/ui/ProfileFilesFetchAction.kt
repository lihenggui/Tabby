package com.github.kr328.clash.profile.ui

internal sealed interface ProfileFilesFetchAction {
  data class Fetch(val documentId: String, val inBaseDirectory: Boolean) : ProfileFilesFetchAction

  data object Ignore : ProfileFilesFetchAction
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
