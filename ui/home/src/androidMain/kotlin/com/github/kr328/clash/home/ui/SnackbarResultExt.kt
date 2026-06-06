package com.github.kr328.clash.home.ui

import androidx.compose.material3.SnackbarResult

internal fun SnackbarResult.toSnackbarActionResult(): SnackbarActionResult {
  return snackbarActionResultFromPlatformActionPerformed(
    actionPerformed = this == SnackbarResult.ActionPerformed
  )
}
