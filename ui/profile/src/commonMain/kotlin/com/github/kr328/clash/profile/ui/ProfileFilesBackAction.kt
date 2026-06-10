package com.github.kr328.clash.profile.ui

internal sealed interface ProfileFilesBackAction {
  data class LeaveDirectory(val location: ProfileFilesLocation) : ProfileFilesBackAction

  data object Finish : ProfileFilesBackAction
}

internal fun profileFilesBackAction(location: ProfileFilesLocation): ProfileFilesBackAction {
  return location.leaveDirectory()?.let(ProfileFilesBackAction::LeaveDirectory)
    ?: ProfileFilesBackAction.Finish
}
