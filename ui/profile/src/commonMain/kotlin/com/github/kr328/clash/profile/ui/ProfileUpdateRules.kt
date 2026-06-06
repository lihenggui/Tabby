package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile

internal fun isProfileUpdatable(profile: Profile): Boolean {
  return profile.imported && profile.type != Profile.Type.File
}

internal fun hasUpdatableProfiles(profiles: List<Profile>): Boolean {
  return profiles.any(::isProfileUpdatable)
}

internal fun filterUpdatableProfiles(profiles: List<Profile>): List<Profile> {
  return profiles.filter(::isProfileUpdatable)
}
