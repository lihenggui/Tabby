package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile
import kotlin.uuid.Uuid

internal enum class ProfileActivationAction {
  Activate,
  RequireSave,
}

internal enum class ProfileUpdateAllAction {
  QueryProfiles,
  Ignore,
}

internal sealed interface ProfilesEventState {
  data object Idle : ProfilesEventState

  data object OpenCreate : ProfilesEventState

  data class OpenEdit(val uuid: Uuid) : ProfilesEventState

  data class ShowMessage(val message: String) : ProfilesEventState

  data class ShowEditableMessage(val message: String, val uuid: Uuid) : ProfilesEventState
}

internal fun profileActivationAction(profile: Profile): ProfileActivationAction {
  return if (profile.imported) ProfileActivationAction.Activate
  else ProfileActivationAction.RequireSave
}

internal fun profileActivationEventState(
  action: ProfileActivationAction,
  profileUuid: Uuid,
  requireSaveMessage: String,
): ProfilesEventState? {
  return when (action) {
    ProfileActivationAction.Activate -> null
    ProfileActivationAction.RequireSave ->
      ProfilesEventState.ShowEditableMessage(requireSaveMessage, profileUuid)
  }
}

internal fun profilesOpenCreateEventState(): ProfilesEventState {
  return ProfilesEventState.OpenCreate
}

internal fun profilesOpenEditEventState(uuid: Uuid): ProfilesEventState {
  return ProfilesEventState.OpenEdit(uuid)
}

internal fun profileUpdateCompletedEventState(message: String): ProfilesEventState {
  return ProfilesEventState.ShowMessage(message)
}

internal fun profileUpdateFailedEventState(
  message: String,
  uuid: Uuid,
): ProfilesEventState {
  return ProfilesEventState.ShowEditableMessage(message, uuid)
}

internal fun profilesConsumedEventState(): ProfilesEventState {
  return ProfilesEventState.Idle
}

internal fun profileUpdateAllAction(state: ProfilesUiState): ProfileUpdateAllAction {
  return if (state.allUpdating) ProfileUpdateAllAction.Ignore
  else ProfileUpdateAllAction.QueryProfiles
}

internal fun isProfileUpdatable(profile: Profile): Boolean {
  return profile.imported && profile.type != Profile.Type.File
}

internal fun hasUpdatableProfiles(profiles: List<Profile>): Boolean {
  return profiles.any(::isProfileUpdatable)
}

internal fun filterUpdatableProfiles(profiles: List<Profile>): List<Profile> {
  return profiles.filter(::isProfileUpdatable)
}

internal fun profileUpdateAllTargets(profiles: List<Profile>): List<Uuid> {
  return filterUpdatableProfiles(profiles).map(Profile::uuid)
}

internal fun profileUpdateFailureReasonText(reason: String?, unknownText: String): String {
  return reason?.takeUnless { it.isBlank() } ?: unknownText
}
