package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile

internal fun hasProfilePropertiesChanges(profile: Profile, original: Profile?): Boolean {
  if (original == null) return false

  return profile.name != original.name ||
    profile.source != original.source ||
    profile.interval != original.interval
}

internal fun toPropertiesProgressState(
  visible: Boolean,
  isIndeterminate: Boolean,
  text: String?,
  progress: Int,
  max: Int,
): PropertiesProgressState {
  return PropertiesProgressState(
    visible = visible,
    isIndeterminate = isIndeterminate,
    text = text,
    progress = progress,
    max = max,
  )
}
