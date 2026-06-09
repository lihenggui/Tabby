package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile
import kotlin.uuid.Uuid

internal enum class ProfileActivationAction {
  Activate,
  RequireSave,
}

enum class ProfileUpdateAllAction {
  QueryProfiles,
  Ignore,
}

internal enum class ProfilesEditableSnackbarAction {
  OpenEdit,
  Ignore,
}

internal sealed interface ProfilesEventState {
  data object Idle : ProfilesEventState

  data object OpenCreate : ProfilesEventState

  data class OpenEdit(val uuid: Uuid) : ProfilesEventState

  data class ShowMessage(val message: String) : ProfilesEventState

  data class ShowEditableMessage(val message: String, val uuid: Uuid) : ProfilesEventState
}

internal sealed interface ProfilesEventRouteEffect {
  data object Ignore : ProfilesEventRouteEffect

  data object OpenCreate : ProfilesEventRouteEffect

  data class OpenEdit(val uuid: Uuid) : ProfilesEventRouteEffect

  data class ShowMessage(val message: String) : ProfilesEventRouteEffect

  data class ShowEditableMessage(val message: String, val uuid: Uuid) : ProfilesEventRouteEffect
}

internal fun profilesInitialEventState(): ProfilesEventState {
  return ProfilesEventState.Idle
}

internal fun profilesEventRouteEffect(eventState: ProfilesEventState): ProfilesEventRouteEffect {
  return when (eventState) {
    ProfilesEventState.Idle -> ProfilesEventRouteEffect.Ignore
    ProfilesEventState.OpenCreate -> ProfilesEventRouteEffect.OpenCreate
    is ProfilesEventState.OpenEdit -> ProfilesEventRouteEffect.OpenEdit(eventState.uuid)
    is ProfilesEventState.ShowMessage -> ProfilesEventRouteEffect.ShowMessage(eventState.message)
    is ProfilesEventState.ShowEditableMessage ->
      ProfilesEventRouteEffect.ShowEditableMessage(eventState.message, eventState.uuid)
  }
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

internal fun profilesEditableSnackbarAction(
  result: ProfileSnackbarActionResult
): ProfilesEditableSnackbarAction {
  return when (result) {
    ProfileSnackbarActionResult.ActionPerformed -> ProfilesEditableSnackbarAction.OpenEdit
    ProfileSnackbarActionResult.Dismissed -> ProfilesEditableSnackbarAction.Ignore
  }
}

internal fun profileUpdateAllAction(state: ProfilesUiState): ProfileUpdateAllAction {
  return profileUpdateAllAction(allUpdating = state.allUpdating)
}

fun profileUpdateAllAction(allUpdating: Boolean): ProfileUpdateAllAction {
  return if (allUpdating) ProfileUpdateAllAction.Ignore else ProfileUpdateAllAction.QueryProfiles
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

fun profileUpdateAllTargets(profiles: List<Profile>): List<Uuid> {
  return filterUpdatableProfiles(profiles).map(Profile::uuid)
}

internal fun profileUpdateFailureReasonText(reason: String?, unknownText: String): String {
  return reason?.takeUnless { it.isBlank() } ?: unknownText
}

internal fun profileUpdateProfileNameText(profileName: String?, unknownText: String): String {
  return profileName?.takeUnless { it.isBlank() } ?: unknownText
}
