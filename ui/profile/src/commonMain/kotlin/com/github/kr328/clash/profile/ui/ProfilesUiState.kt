package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile

internal data class ProfilesUiState(
  val profiles: List<Profile> = emptyList(),
  val allUpdating: Boolean = false,
  val hasUpdatableProfile: Boolean = false,
  val currentTime: Long = 0,
)

internal fun ProfilesUiState.withProfiles(profiles: List<Profile>): ProfilesUiState {
  return copy(
    profiles = profiles,
    hasUpdatableProfile = hasUpdatableProfiles(profiles),
  )
}

internal fun ProfilesUiState.withAllUpdating(updating: Boolean): ProfilesUiState {
  return copy(allUpdating = updating)
}

internal fun ProfilesUiState.withCurrentTime(currentTime: Long): ProfilesUiState {
  return copy(currentTime = currentTime)
}
