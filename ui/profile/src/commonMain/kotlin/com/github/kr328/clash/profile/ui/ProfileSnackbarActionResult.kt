package com.github.kr328.clash.profile.ui

import androidx.compose.material3.SnackbarResult

internal enum class ProfileSnackbarActionResult {
  ActionPerformed,
  Dismissed,
}

internal fun SnackbarResult.toProfileSnackbarActionResult(): ProfileSnackbarActionResult {
  return profileSnackbarActionResultFromPlatformActionPerformed(
    actionPerformed = this == SnackbarResult.ActionPerformed
  )
}

internal fun profileSnackbarActionResultFromPlatformActionPerformed(
  actionPerformed: Boolean
): ProfileSnackbarActionResult {
  return if (actionPerformed) {
    ProfileSnackbarActionResult.ActionPerformed
  } else {
    ProfileSnackbarActionResult.Dismissed
  }
}
