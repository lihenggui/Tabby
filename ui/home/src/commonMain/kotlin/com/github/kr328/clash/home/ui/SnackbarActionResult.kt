package com.github.kr328.clash.home.ui

import androidx.compose.material3.SnackbarResult

internal enum class SnackbarActionResult {
  ActionPerformed,
  Dismissed,
}

internal fun SnackbarResult.toSnackbarActionResult(): SnackbarActionResult {
  return if (this == SnackbarResult.ActionPerformed) {
    SnackbarActionResult.ActionPerformed
  } else {
    SnackbarActionResult.Dismissed
  }
}
