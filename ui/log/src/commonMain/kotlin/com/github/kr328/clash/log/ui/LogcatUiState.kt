package com.github.kr328.clash.log.ui

import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.log.model.LogFile

internal data class LogcatUiState(
  val streaming: Boolean = true,
  val messages: List<LogMessage> = emptyList(),
  val exportProgress: LogcatExportProgress = LogcatExportProgress(),
)

internal data class LogcatExportProgress(
  val visible: Boolean = false,
  val isIndeterminate: Boolean = true,
  val text: String? = null,
  val progress: Int = 0,
  val max: Int = 0,
)

internal sealed interface LogcatInitialAction {
  data object StartStreaming : LogcatInitialAction

  data class LoadFile(val file: LogFile) : LogcatInitialAction

  data object InvalidFile : LogcatInitialAction
}

internal fun logcatInitialAction(fileName: String?): LogcatInitialAction {
  val file = fileName?.let(LogFile::parse)
  return when {
    fileName == null -> LogcatInitialAction.StartStreaming
    file != null -> LogcatInitialAction.LoadFile(file)
    else -> LogcatInitialAction.InvalidFile
  }
}

internal fun LogcatUiState.withStreaming(streaming: Boolean): LogcatUiState {
  return copy(streaming = streaming)
}

internal fun LogcatUiState.withMessages(messages: List<LogMessage>): LogcatUiState {
  return copy(messages = messages)
}

internal fun LogcatUiState.withExportStarted(max: Int): LogcatUiState {
  return copy(
    exportProgress =
      LogcatExportProgress(
        visible = true,
        isIndeterminate = true,
        progress = 0,
        max = max,
      )
  )
}

internal fun LogcatUiState.withExportProgress(progress: Int): LogcatUiState {
  return copy(
    exportProgress =
      exportProgress.copy(
        isIndeterminate = false,
        progress = progress,
      )
  )
}

internal fun LogcatUiState.withExportFinished(): LogcatUiState {
  return copy(exportProgress = LogcatExportProgress())
}
