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

internal enum class LogcatCloseAction {
  StopStreamingAndOpenLogs,
  CloseViewer,
}

internal sealed interface LogcatDeleteAction {
  data class DeleteFile(val file: LogFile) : LogcatDeleteAction

  data object Ignore : LogcatDeleteAction
}

internal sealed interface LogcatRequestExportAction {
  data class RequestExport(val fileName: String) : LogcatRequestExportAction

  data object Ignore : LogcatRequestExportAction
}

internal sealed interface LogcatExportAction {
  data class ExportFile(val file: LogFile) : LogcatExportAction

  data object Ignore : LogcatExportAction
}

internal sealed interface LogcatPollAction {
  data class QuerySnapshot(val initialSnapshot: Boolean) : LogcatPollAction

  data object Ignore : LogcatPollAction
}

internal data class LogcatSnapshotAction(
  val state: LogcatUiState,
  val initialSnapshot: Boolean,
)

internal fun logcatInitialAction(fileName: String?): LogcatInitialAction {
  val file = fileName?.let(LogFile::parse)
  return when {
    fileName == null -> LogcatInitialAction.StartStreaming
    file != null -> LogcatInitialAction.LoadFile(file)
    else -> LogcatInitialAction.InvalidFile
  }
}

internal fun logcatCloseAction(state: LogcatUiState): LogcatCloseAction {
  return if (state.streaming) LogcatCloseAction.StopStreamingAndOpenLogs
  else LogcatCloseAction.CloseViewer
}

internal fun logcatDeleteAction(currentFile: LogFile?): LogcatDeleteAction {
  return currentFile?.let(LogcatDeleteAction::DeleteFile) ?: LogcatDeleteAction.Ignore
}

internal fun logcatRequestExportAction(currentFile: LogFile?): LogcatRequestExportAction {
  return currentFile?.let { LogcatRequestExportAction.RequestExport(it.fileName) }
    ?: LogcatRequestExportAction.Ignore
}

internal fun logcatExportAction(
  currentFile: LogFile?,
  hasDestination: Boolean,
): LogcatExportAction {
  val file = currentFile ?: return LogcatExportAction.Ignore
  if (!hasDestination) return LogcatExportAction.Ignore

  return LogcatExportAction.ExportFile(file)
}

internal fun logcatPollAction(
  started: Boolean,
  initialSnapshot: Boolean,
): LogcatPollAction {
  return if (started) {
    LogcatPollAction.QuerySnapshot(initialSnapshot)
  } else {
    LogcatPollAction.Ignore
  }
}

internal fun logcatSnapshotAction(
  state: LogcatUiState,
  initialSnapshot: Boolean,
  messages: List<LogMessage>?,
): LogcatSnapshotAction {
  return if (messages == null) {
    LogcatSnapshotAction(
      state = state,
      initialSnapshot = initialSnapshot,
    )
  } else {
    LogcatSnapshotAction(
      state = state.withMessages(messages),
      initialSnapshot = false,
    )
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
