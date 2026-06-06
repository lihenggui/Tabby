package com.github.kr328.clash.profile.ui

import androidx.compose.material3.SnackbarResult

internal fun SnackbarResult.toProfileSnackbarActionResult(): ProfileSnackbarActionResult {
  return profileSnackbarActionResultFromPlatformActionPerformed(
    actionPerformed = this == SnackbarResult.ActionPerformed
  )
}
