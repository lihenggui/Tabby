package com.github.kr328.clash.profile.ui

internal enum class ProfileSnackbarActionResult {
  ActionPerformed,
  Dismissed,
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
