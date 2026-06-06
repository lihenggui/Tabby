package com.github.kr328.clash.home.ui

import androidx.compose.material3.SnackbarResult

internal fun SnackbarResult.toSnackbarActionResult(): SnackbarActionResult {
  return when (this) {
    SnackbarResult.ActionPerformed -> SnackbarActionResult.ActionPerformed
    SnackbarResult.Dismissed -> SnackbarActionResult.Dismissed
  }
}
