package com.github.kr328.clash.log.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.log.model.LogFile
import com.github.kr328.clash.ui.component.ModelProgressBarDialog
import org.jetbrains.compose.resources.stringResource
import tabby.ui.log.generated.resources.Res as LogRes
import tabby.ui.log.generated.resources.invalid_log_file

@Composable
fun LogcatRouteContent(
  fileName: String?,
  modifier: Modifier = Modifier,
  streaming: Boolean = fileName == null,
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
  onCopyMessage: (LogMessage) -> Unit = {},
) {
  val listState = rememberLazyListState()
  val snackbarHostState = remember { SnackbarHostState() }
  val invalidFileTip = stringResource(LogRes.string.invalid_log_file)
  val currentOnInvalidFile = rememberUpdatedState(onInvalidFile)
  val currentFile = remember(fileName) { fileName?.let(LogFile::parse) }

  LaunchedEffect(fileName, invalidFileTip) {
    if (logcatInitialAction(fileName) == LogcatInitialAction.InvalidFile) {
      snackbarHostState.showSnackbar(message = invalidFileTip)
      currentOnInvalidFile.value()
    }
  }

  LaunchedEffect(listState, streaming) {
    if (!streaming) return@LaunchedEffect

    snapshotFlow { messages.size }
      .collect { size ->
        if (logcatShouldAutoScrollToLatest(size, listState.isLogcatViewportAtBottom())) {
          listState.animateScrollToItem(size - 1)
        }
      }
  }

  LogcatContent(
    modifier = modifier,
    streaming = streaming,
    messages = messages,
    listState = listState,
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
    onCopyMessage = onCopyMessage,
  )

  ModelProgressBarDialog(
    visible = exportProgressVisible,
    isIndeterminate = exportProgressIndeterminate,
    text = exportProgressText,
    progress = exportProgress,
    max = exportProgressMax,
  )
}
