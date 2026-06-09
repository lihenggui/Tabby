package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile

internal enum class ProfileTypeTextToken {
  File,
  Url,
  External,
}

internal data class ProfileTypeText(
  val token: ProfileTypeTextToken,
  val pending: Boolean,
)

internal fun profileTypeText(
  type: Profile.Type,
  pending: Boolean,
): ProfileTypeText {
  val token =
    when (type) {
      Profile.Type.File -> ProfileTypeTextToken.File
      Profile.Type.Url -> ProfileTypeTextToken.Url
      Profile.Type.External -> ProfileTypeTextToken.External
    }

  return ProfileTypeText(token = token, pending = pending)
}

internal fun <T> profileTypeTextResourceToken(
  token: ProfileTypeTextToken,
  file: T,
  url: T,
  external: T,
): T {
  return when (token) {
    ProfileTypeTextToken.File -> file
    ProfileTypeTextToken.Url -> url
    ProfileTypeTextToken.External -> external
  }
}

internal fun Profile.toProfileListItem(
  currentTime: Long,
  formatTypeText: (ProfileTypeText) -> String,
  formatBytes: (Long) -> String,
  formatExpire: (Long) -> String,
  formatElapsedMillis: (Long) -> String,
): ProfileListItem {
  val showTraffic = showsTrafficUsage()
  val usedTraffic = download + upload

  return ProfileListItem(
    profile = this,
    typeText = formatTypeText(profileTypeText(type = type, pending = pending)),
    usageText =
      if (showTraffic) {
        "${formatBytes(usedTraffic)} / ${formatBytes(total)}"
      } else {
        null
      },
    expireText = expire.takeIf { it != 0L }?.let(formatExpire),
    updatedAtText = formatElapsedMillis(currentTime - updatedAt),
    trafficProgress = if (showTraffic) trafficProgress(usedTraffic = usedTraffic) else 0,
  )
}

internal fun ProfilesUiState.toProfileListItems(
  formatTypeText: (ProfileTypeText) -> String,
  formatBytes: (Long) -> String,
  formatExpire: (Long) -> String,
  formatElapsedMillis: (Long) -> String,
): List<ProfileListItem> {
  return profiles.map { profile ->
    profile.toProfileListItem(
      currentTime = currentTime,
      formatTypeText = formatTypeText,
      formatBytes = formatBytes,
      formatExpire = formatExpire,
      formatElapsedMillis = formatElapsedMillis,
    )
  }
}
