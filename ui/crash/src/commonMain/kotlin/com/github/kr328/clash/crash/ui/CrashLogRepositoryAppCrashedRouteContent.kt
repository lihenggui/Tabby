package com.github.kr328.clash.crash.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.kr328.clash.crash.model.CrashLogRepository

@Composable
fun CrashLogRepositoryAppCrashedRouteContent(
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
        logs = appCrashLogLoadFailureMessage(cause)
      }
  }

  AppCrashedRouteContent(
    modifier = modifier,
    logs = logs,
  )
}
