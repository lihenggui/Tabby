package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile

internal sealed interface ProfileFilesInitAction {
  data class Initialize(val location: ProfileFilesLocation) : ProfileFilesInitAction

  data object Ignore : ProfileFilesInitAction
}

internal sealed interface ProfileFilesLoadedAction {
  data class LoadFiles(val configurationEditable: Boolean) : ProfileFilesLoadedAction

  data object Finish : ProfileFilesLoadedAction
}

internal fun profileFilesInitAction(
  location: ProfileFilesLocation,
  rootDocumentId: String,
): ProfileFilesInitAction {
  return if (location.initialized) {
    ProfileFilesInitAction.Ignore
  } else {
    ProfileFilesInitAction.Initialize(location.initialize(rootDocumentId))
  }
}

internal fun profileFilesLoadedAction(profile: Profile?): ProfileFilesLoadedAction {
  return profile?.let {
    ProfileFilesLoadedAction.LoadFiles(isProfileConfigurationEditable(it))
  } ?: ProfileFilesLoadedAction.Finish
}
