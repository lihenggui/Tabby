package com.github.kr328.clash.profile.ui

import androidx.compose.material3.SnackbarResult

internal enum class ProfileSnackbarActionResult {
  ActionPerformed,
  Dismissed,
}

internal fun SnackbarResult.toProfileSnackbarActionResult(): ProfileSnackbarActionResult {
  return when (this) {
    SnackbarResult.ActionPerformed -> ProfileSnackbarActionResult.ActionPerformed
    SnackbarResult.Dismissed -> ProfileSnackbarActionResult.Dismissed
  }
}
