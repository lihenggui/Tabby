package com.github.kr328.clash.crash.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppCrashedRouteContent(
  logs: String,
  modifier: Modifier = Modifier,
) {
  AppCrashedContent(
    modifier = modifier,
    logs = logs,
  )
}
