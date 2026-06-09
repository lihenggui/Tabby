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
import tabby.ui.log.generated.resources.invalid_log_file

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
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  formatMessageTime: (Long) -> String = { "" },
  onClose: () -> Unit,
  onDelete: () -> Unit,
  onExport: () -> Unit,
  copyMessageText: suspend (label: String, text: String) -> Unit = { _, _ -> },
) {
  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()
  val messageCount = rememberUpdatedState(state.messages.size)
  val currentCopyMessageText = rememberUpdatedState(copyMessageText)
  val copiedMessage = stringResource(LogRes.string.copied)

  LaunchedEffect(listState, state.streaming) {
    if (!state.streaming) return@LaunchedEffect

    snapshotFlow { messageCount.value }
      .collect { size ->
        if (logcatShouldAutoScrollToLatest(size, listState.isLogcatViewportAtBottom())) {
          listState.animateScrollToItem(size - 1)
        }
      }
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
