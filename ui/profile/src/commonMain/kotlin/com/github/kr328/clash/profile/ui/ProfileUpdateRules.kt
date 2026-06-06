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

internal fun profileActivationAction(profile: Profile): ProfileActivationAction {
  return if (profile.imported) ProfileActivationAction.Activate
  else ProfileActivationAction.RequireSave
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
