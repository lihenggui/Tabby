package com.github.kr328.clash.log.ui

import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.log.model.LogFile

const val LOGCAT_MESSAGES_CAPACITY = 128

internal data class LogcatUiState(
  val streaming: Boolean = true,
  val messages: List<LogMessage> = emptyList(),
  val exportProgress: LogcatExportProgress = LogcatExportProgress(),
)

internal fun logcatInitialUiState(): LogcatUiState {
  return LogcatUiState()
}

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

internal sealed interface LogcatEventState {
  data object Idle : LogcatEventState

  data object Close : LogcatEventState

  data object InvalidFile : LogcatEventState

  data object OpenLogs : LogcatEventState

  data class RequestExport(val fileName: String) : LogcatEventState

  data class ExportResult(val success: Boolean, val errorMessage: String?) : LogcatEventState

  data class ShowMessage(val message: String) : LogcatEventState
}

internal sealed interface LogcatEventRouteEffect {
  data object Ignore : LogcatEventRouteEffect

  data object Close : LogcatEventRouteEffect

  data object InvalidFile : LogcatEventRouteEffect

  data object OpenLogs : LogcatEventRouteEffect

  data class RequestExport(val fileName: String) : LogcatEventRouteEffect

  data class ExportResult(val success: Boolean, val errorMessage: String?) : LogcatEventRouteEffect

  data class ShowMessage(val message: String) : LogcatEventRouteEffect
}

internal fun logcatInitialEventState(): LogcatEventState {
  return LogcatEventState.Idle
}

internal fun logcatEventRouteEffect(eventState: LogcatEventState): LogcatEventRouteEffect {
  return when (eventState) {
    LogcatEventState.Idle -> LogcatEventRouteEffect.Ignore
    LogcatEventState.Close -> LogcatEventRouteEffect.Close
    LogcatEventState.InvalidFile -> LogcatEventRouteEffect.InvalidFile
    LogcatEventState.OpenLogs -> LogcatEventRouteEffect.OpenLogs
    is LogcatEventState.RequestExport -> LogcatEventRouteEffect.RequestExport(eventState.fileName)
    is LogcatEventState.ExportResult ->
      LogcatEventRouteEffect.ExportResult(
        success = eventState.success,
        errorMessage = eventState.errorMessage,
      )
    is LogcatEventState.ShowMessage -> LogcatEventRouteEffect.ShowMessage(eventState.message)
  }
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

internal sealed interface LogcatExportDestinationAction<out DestinationT> {
  data class ExportFile<out DestinationT>(val file: LogFile, val destination: DestinationT) :
    LogcatExportDestinationAction<DestinationT>

  data object Ignore : LogcatExportDestinationAction<Nothing>
}

internal data class LogcatExportPlatformSpec(val exportMimeType: String)

internal data class LogcatExportResult(val destinationSelected: Boolean)

internal data class LogcatCopyMessagePayload(
  val label: String,
  val text: String,
)

internal fun logcatExportPlatformSpec(): LogcatExportPlatformSpec {
  return LogcatExportPlatformSpec(exportMimeType = "text/plain")
}

internal fun logcatExportResult(destinationSelected: Boolean): LogcatExportResult {
  return LogcatExportResult(destinationSelected = destinationSelected)
}

internal fun logcatExportActionFromDestinationSelection(
  currentFile: LogFile?,
  destinationSelected: Boolean,
): LogcatExportAction {
  return logcatExportAction(
    currentFile = currentFile,
    result = logcatExportResult(destinationSelected = destinationSelected),
  )
}

internal fun <DestinationT> logcatExportDestinationAction(
  currentFile: LogFile?,
  destination: DestinationT?,
): LogcatExportDestinationAction<DestinationT> {
  val file = currentFile ?: return LogcatExportDestinationAction.Ignore
  val selectedDestination = destination ?: return LogcatExportDestinationAction.Ignore

  return LogcatExportDestinationAction.ExportFile(
    file = file,
    destination = selectedDestination,
  )
}

internal fun logcatCopyMessagePayload(message: LogMessage): LogcatCopyMessagePayload {
  return logcatCopyMessagePayload(message.message)
}

internal fun logcatCopyMessagePayload(messageText: String): LogcatCopyMessagePayload {
  return LogcatCopyMessagePayload(
    label = "log_message",
    text = messageText,
  )
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

internal fun logcatStreamingFromInitialAction(action: LogcatInitialAction): Boolean {
  return when (action) {
    LogcatInitialAction.StartStreaming -> true
    is LogcatInitialAction.LoadFile,
    LogcatInitialAction.InvalidFile -> false
  }
}

internal fun logcatInitialStreaming(fileName: String?): Boolean {
  return logcatStreamingFromInitialAction(logcatInitialAction(fileName))
}

internal fun logcatFileFromInitialAction(action: LogcatInitialAction): LogFile? {
  return when (action) {
    is LogcatInitialAction.LoadFile -> action.file
    LogcatInitialAction.StartStreaming,
    LogcatInitialAction.InvalidFile -> null
  }
}

internal fun logcatInitialFile(fileName: String?): LogFile? {
  return logcatFileFromInitialAction(logcatInitialAction(fileName))
}

internal fun logcatCloseAction(state: LogcatUiState): LogcatCloseAction {
  return if (state.streaming) LogcatCloseAction.StopStreamingAndOpenLogs
  else LogcatCloseAction.CloseViewer
}

internal fun logcatCloseEventState(action: LogcatCloseAction): LogcatEventState {
  return when (action) {
    LogcatCloseAction.StopStreamingAndOpenLogs -> LogcatEventState.OpenLogs
    LogcatCloseAction.CloseViewer -> LogcatEventState.Close
  }
}

internal fun logcatInitialEventState(action: LogcatInitialAction): LogcatEventState? {
  return when (action) {
    LogcatInitialAction.InvalidFile -> LogcatEventState.InvalidFile
    is LogcatInitialAction.LoadFile,
    LogcatInitialAction.StartStreaming -> null
  }
}

internal fun logcatLoadFileFailureEventState(): LogcatEventState {
  return LogcatEventState.InvalidFile
}

internal fun logcatStartStreamingFailureEventState(): LogcatEventState {
  return LogcatEventState.OpenLogs
}

internal fun logcatConsumedEventState(): LogcatEventState {
  return LogcatEventState.Idle
}

fun logcatMessagesAfterAppend(
  messages: List<LogMessage>,
  message: LogMessage,
  capacity: Int = LOGCAT_MESSAGES_CAPACITY,
): List<LogMessage> {
  require(capacity > 0) { "capacity must be positive" }

  return (messages + message).takeLast(capacity)
}

internal fun logcatDeleteAction(currentFile: LogFile?): LogcatDeleteAction {
  return currentFile?.let(LogcatDeleteAction::DeleteFile) ?: LogcatDeleteAction.Ignore
}

internal fun logcatDeleteEventState(action: LogcatDeleteAction): LogcatEventState? {
  return when (action) {
    is LogcatDeleteAction.DeleteFile -> LogcatEventState.Close
    LogcatDeleteAction.Ignore -> null
  }
}

internal fun logcatRequestExportAction(currentFile: LogFile?): LogcatRequestExportAction {
  return currentFile?.let { LogcatRequestExportAction.RequestExport(it.fileName) }
    ?: LogcatRequestExportAction.Ignore
}

internal fun logcatRequestExportEventState(action: LogcatRequestExportAction): LogcatEventState? {
  return when (action) {
    is LogcatRequestExportAction.RequestExport -> LogcatEventState.RequestExport(action.fileName)
    LogcatRequestExportAction.Ignore -> null
  }
}

internal fun logcatExportAction(
  currentFile: LogFile?,
  result: LogcatExportResult,
): LogcatExportAction {
  val file = currentFile ?: return LogcatExportAction.Ignore
  if (!result.destinationSelected) return LogcatExportAction.Ignore

  return LogcatExportAction.ExportFile(file)
}

internal fun logcatExportResultEventState(
  success: Boolean,
  errorMessage: String?,
): LogcatEventState {
  return LogcatEventState.ExportResult(
    success = success,
    errorMessage = errorMessage,
  )
}

internal fun logcatExportResultMessage(
  success: Boolean,
  errorMessage: String?,
  exportedMessage: String,
  unknownMessage: String,
): String {
  return if (success) exportedMessage else errorMessage ?: unknownMessage
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

internal fun logcatStartedStateFromStartStopEvent(
  currentStarted: Boolean,
  isStartEvent: Boolean,
  isStopEvent: Boolean,
): Boolean {
  return when {
    isStartEvent -> true
    isStopEvent -> false
    else -> currentStarted
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

internal fun LogcatUiState.withInitialAction(action: LogcatInitialAction): LogcatUiState {
  return withStreaming(logcatStreamingFromInitialAction(action))
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
