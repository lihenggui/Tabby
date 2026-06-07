package com.github.kr328.clash.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.kr328.clash.crash.model.CrashLogRepository
import com.github.kr328.clash.crash.ui.AppCrashedRouteContent

@Composable
internal fun CrashLogRepositoryAppCrashedRouteContent(
  crashLogRepository: CrashLogRepository,
  modifier: Modifier = Modifier,
  onActionError: (Throwable) -> Unit = {},
) {
  var logs by remember(crashLogRepository) { mutableStateOf("") }

  LaunchedEffect(crashLogRepository) {
    runCatching { crashLogRepository.loadCrashLog() }
      .onSuccess { logs = it }
      .onFailure { cause ->
        onActionError(cause)
        logs = tabbyCrashLogLoadFailureMessage(cause)
      }
  }

  AppCrashedRouteContent(
    modifier = modifier,
    logs = logs,
  )
}

internal fun tabbyCrashLogLoadFailureMessage(cause: Throwable): String {
  return "Failed to load crash logs: $cause"
}
