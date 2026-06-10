package com.github.kr328.clash.log.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.log.model.LogFile
import com.github.kr328.clash.ui.component.ModelProgressBarDialog
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tabby.ui.log.generated.resources.Res as LogRes
import tabby.ui.log.generated.resources.copied
import tabby.ui.log.generated.resources.file_exported
import tabby.ui.log.generated.resources.invalid_log_file
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.unknown

@Composable
fun LogcatRouteContent(
  fileName: String?,
  modifier: Modifier = Modifier,
  streaming: Boolean = logcatInitialStreaming(fileName),
  messages: List<LogMessage> = emptyList(),
  exportProgressVisible: Boolean = false,
  exportProgressIndeterminate: Boolean = true,
  exportProgressText: String? = null,
  exportProgress: Int = 0,
  exportProgressMax: Int = 0,
  formatMessageTime: (Long) -> String = { "" },
  onOpenLogs: () -> Unit = {},
  onInvalidFile: () -> Unit = {},
  onClose: () -> Unit = {},
  onDeleteFile: (LogFile) -> Unit = {},
  onRequestExport: (String) -> Unit = {},
  copyMessageText: suspend (label: String, text: String) -> Unit = { _, _ -> },
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val invalidFileTip = stringResource(LogRes.string.invalid_log_file)
  val currentOnInvalidFile = rememberUpdatedState(onInvalidFile)
  val initialAction = remember(fileName) { logcatInitialAction(fileName) }
  val currentFile = remember(initialAction) { logcatFileFromInitialAction(initialAction) }

  LaunchedEffect(fileName, invalidFileTip) {
    if (logcatInitialEventState(initialAction) == LogcatEventState.InvalidFile) {
      snackbarHostState.showSnackbar(message = invalidFileTip)
      currentOnInvalidFile.value()
    }
  }

  LogcatStateRouteContent(
    modifier = modifier,
    state =
      LogcatUiState(
        streaming = streaming,
        messages = messages,
        exportProgress =
          LogcatExportProgress(
            visible = exportProgressVisible,
            isIndeterminate = exportProgressIndeterminate,
            text = exportProgressText,
            progress = exportProgress,
            max = exportProgressMax,
          ),
      ),
    snackbarHostState = snackbarHostState,
    formatMessageTime = formatMessageTime,
    onClose = {
      when (logcatCloseAction(LogcatUiState(streaming = streaming))) {
        LogcatCloseAction.StopStreamingAndOpenLogs -> onOpenLogs()
        LogcatCloseAction.CloseViewer -> onClose()
      }
    },
    onDelete = {
      when (val action = logcatDeleteAction(currentFile)) {
        is LogcatDeleteAction.DeleteFile -> {
          onDeleteFile(action.file)
          when (logcatDeleteEventState(action)) {
            LogcatEventState.Close -> onClose()
            LogcatEventState.Idle,
            LogcatEventState.InvalidFile,
            LogcatEventState.OpenLogs,
            is LogcatEventState.RequestExport,
            is LogcatEventState.ExportResult,
            is LogcatEventState.ShowMessage,
            null -> Unit
          }
        }
        LogcatDeleteAction.Ignore -> Unit
      }
    },
    onExport = {
      when (val action = logcatRequestExportAction(currentFile)) {
        is LogcatRequestExportAction.RequestExport -> onRequestExport(action.fileName)
        LogcatRequestExportAction.Ignore -> Unit
      }
    },
    copyMessageText = copyMessageText,
  )
}

@Composable
internal fun LogcatStateRouteContent(
  modifier: Modifier = Modifier,
  state: LogcatUiState,
  eventState: LogcatEventState = logcatInitialEventState(),
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  formatMessageTime: (Long) -> String = { "" },
  onClose: () -> Unit,
  onRouteClose: () -> Unit = {},
  onDelete: () -> Unit,
  onExport: () -> Unit,
  onRouteOpenLogs: () -> Unit = {},
  onRouteInvalidFile: () -> Unit = {},
  onRouteRequestExport: (String) -> Unit = {},
  onRouteEventConsumed: () -> Unit = {},
  copyMessageText: suspend (label: String, text: String) -> Unit = { _, _ -> },
) {
  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()
  val messageCount = rememberUpdatedState(state.messages.size)
  val currentCopyMessageText = rememberUpdatedState(copyMessageText)
  val currentOnRouteClose = rememberUpdatedState(onRouteClose)
  val currentOnRouteOpenLogs = rememberUpdatedState(onRouteOpenLogs)
  val currentOnRouteInvalidFile = rememberUpdatedState(onRouteInvalidFile)
  val currentOnRouteRequestExport = rememberUpdatedState(onRouteRequestExport)
  val currentOnRouteEventConsumed = rememberUpdatedState(onRouteEventConsumed)
  val copiedMessage = stringResource(LogRes.string.copied)
  val invalidFileTip = stringResource(LogRes.string.invalid_log_file)
  val exportedMessage = stringResource(LogRes.string.file_exported)
  val unknownMessage = stringResource(SharedRes.string.unknown)

  LaunchedEffect(listState, state.streaming) {
    if (!state.streaming) return@LaunchedEffect

    snapshotFlow { messageCount.value }
      .collect { size ->
        if (logcatShouldAutoScrollToLatest(size, listState.isLogcatViewportAtBottom())) {
          listState.animateScrollToItem(size - 1)
        }
      }
  }

  LaunchedEffect(eventState, invalidFileTip, exportedMessage, unknownMessage) {
    when (val action = logcatEventRouteEffect(eventState)) {
      LogcatEventRouteEffect.Ignore -> return@LaunchedEffect
      LogcatEventRouteEffect.Close -> currentOnRouteClose.value()
      LogcatEventRouteEffect.InvalidFile -> {
        snackbarHostState.showSnackbar(message = invalidFileTip)
        currentOnRouteInvalidFile.value()
      }
      LogcatEventRouteEffect.OpenLogs -> currentOnRouteOpenLogs.value()
      is LogcatEventRouteEffect.RequestExport -> currentOnRouteRequestExport.value(action.fileName)
      is LogcatEventRouteEffect.ExportResult -> {
        snackbarHostState.showSnackbar(
          message =
            logcatExportResultMessage(
              success = action.success,
              errorMessage = action.errorMessage,
              exportedMessage = exportedMessage,
              unknownMessage = unknownMessage,
            ),
          withDismissAction = true,
        )
      }
      is LogcatEventRouteEffect.ShowMessage -> {
        snackbarHostState.showSnackbar(message = action.message, withDismissAction = true)
      }
    }

    currentOnRouteEventConsumed.value()
  }

  LogcatContent(
    modifier = modifier,
    streaming = state.streaming,
    messages = state.messages,
    listState = listState,
    snackbarHostState = snackbarHostState,
    formatMessageTime = formatMessageTime,
    onClose = onClose,
    onDelete = onDelete,
    onExport = onExport,
    onCopyMessage = { message ->
      scope.launch {
        val payload = logcatCopyMessagePayload(message)
        currentCopyMessageText.value(payload.label, payload.text)
        snackbarHostState.showSnackbar(message = copiedMessage, withDismissAction = true)
      }
    },
  )

  ModelProgressBarDialog(
    visible = state.exportProgress.visible,
    isIndeterminate = state.exportProgress.isIndeterminate,
    text = state.exportProgress.text,
    progress = state.exportProgress.progress,
    max = state.exportProgress.max,
  )
}
