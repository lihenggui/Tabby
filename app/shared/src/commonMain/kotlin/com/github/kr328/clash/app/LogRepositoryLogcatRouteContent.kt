package com.github.kr328.clash.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.engine.api.LogRepository
import com.github.kr328.clash.log.ui.LogcatRouteContent
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

@Composable
internal fun LogRepositoryLogcatRouteContent(
  logRepository: LogRepository,
  fileName: String?,
  onOpenLogs: () -> Unit,
  onInvalidFile: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
  onActionError: (Throwable) -> Unit = {},
) {
  val streaming = fileName == null
  var messages by remember(logRepository, fileName) { mutableStateOf(emptyList<LogMessage>()) }

  LaunchedEffect(logRepository, streaming) {
    messages = emptyList()

    if (!streaming) {
      return@LaunchedEffect
    }

    logRepository
      .observeLogs()
      .catch { cause -> onActionError(cause) }
      .collect { message -> messages = tabbyLogcatMessagesAfterAppend(messages, message) }
  }

  LogcatRouteContent(
    modifier = modifier,
    fileName = fileName,
    streaming = streaming,
    messages = messages,
    onOpenLogs = onOpenLogs,
    onInvalidFile = onInvalidFile,
    onClose = onClose,
  )
}
