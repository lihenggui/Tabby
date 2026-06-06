package com.github.kr328.clash.home.ui

internal enum class SnackbarActionResult {
  ActionPerformed,
  Dismissed,
}

internal fun snackbarActionResultFromPlatformActionPerformed(
  actionPerformed: Boolean
): SnackbarActionResult {
  return if (actionPerformed) {
    SnackbarActionResult.ActionPerformed
  } else {
    SnackbarActionResult.Dismissed
  }
}
