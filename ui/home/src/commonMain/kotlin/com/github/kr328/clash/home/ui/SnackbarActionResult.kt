package com.github.kr328.clash.home.ui

import androidx.compose.material3.SnackbarResult

internal enum class SnackbarActionResult {
  ActionPerformed,
  Dismissed,
}

internal fun SnackbarResult.toSnackbarActionResult(): SnackbarActionResult {
  return snackbarActionResultFromPlatformActionPerformed(
    actionPerformed = this == SnackbarResult.ActionPerformed
  )
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
