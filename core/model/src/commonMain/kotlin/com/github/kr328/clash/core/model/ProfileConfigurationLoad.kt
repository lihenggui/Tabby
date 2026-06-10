package com.github.kr328.clash.core.model

import kotlin.uuid.Uuid

fun profileShouldLoadConfiguration(
  currentProfile: Uuid,
  loadedProfile: Uuid?,
  changedProfile: Uuid?,
): Boolean {
  return currentProfile != loadedProfile ||
    changedProfile == null ||
    changedProfile == loadedProfile
}
