package com.github.kr328.clash.log.ui

import android.content.ClipData
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.glue.util.format
import com.github.kr328.clash.log.vm.LogcatViewModel
import com.github.kr328.clash.ui.component.ModelProgressBarDialog
import com.github.kr328.clash.ui.lifecycle.viewModelWithLifecycle
import java.util.Date
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tabby.ui.log.generated.resources.Res as LogRes
import tabby.ui.log.generated.resources.copied
import tabby.ui.log.generated.resources.invalid_log_file

@Composable
internal fun LogcatScreen(
  fileName: String?,
  modifier: Modifier = Modifier,
  viewModel: LogcatViewModel = viewModelWithLifecycle(),
  onOpenLogs: () -> Unit,
  onInvalidFile: () -> Unit,
  onClose: () -> Unit,
) {
  val clipboard = LocalClipboard.current
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val evenState by viewModel.eventState.collectAsStateWithLifecycle()
  val listState = rememberLazyListState()
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val messageCopied = stringResource(LogRes.string.copied)
  val invalidFileTip = stringResource(LogRes.string.invalid_log_file)

  LaunchedEffect(fileName, viewModel) { viewModel.init(fileName) }

  val exportLauncher =
    rememberLauncherForActivityResult(CreateDocument("text/plain")) { uri ->
      viewModel.exportTo(uri)
    }

  LaunchedEffect(evenState) {
    when (val event = evenState) {
      Idle -> Unit
      Close -> onClose()
      InvalidFile -> {
        snackbarHostState.showSnackbar(message = invalidFileTip)
        onInvalidFile()
      }
      OpenLogs -> onOpenLogs()
      is RequestExport -> exportLauncher.launch(event.fileName)
      is ShowMessage -> {
        snackbarHostState.showSnackbar(message = event.message, withDismissAction = true)
      }
    }
    viewModel.consumeEvent()
  }

  LaunchedEffect(listState, uiState.streaming) {
    if (!uiState.streaming) return@LaunchedEffect

    snapshotFlow { uiState.messages.size }
      .collect { size ->
        if (size > 0 && listState.isBottom) {
          listState.animateScrollToItem(size - 1)
        }
      }
  }

  LogcatContent(
    modifier = modifier,
    streaming = uiState.streaming,
    messages = uiState.messages,
    listState = listState,
    snackbarHostState = snackbarHostState,
    formatMessageTime = { time ->
      Date(time).format(context, includeDate = false, includeTime = true)
    },
    onClose = viewModel::close,
    onDelete = viewModel::delete,
    onExport = viewModel::requestExport,
    onCopyMessage = { message ->
      scope.launch {
        val clipEntry = ClipData.newPlainText("log_message", message.message).toClipEntry()
        clipboard.setClipEntry(clipEntry)
        snackbarHostState.showSnackbar(message = messageCopied, withDismissAction = true)
      }
    },
  )

  ModelProgressBarDialog(
    visible = uiState.exportProgress.visible,
    isIndeterminate = uiState.exportProgress.isIndeterminate,
    text = uiState.exportProgress.text,
    progress = uiState.exportProgress.progress,
    max = uiState.exportProgress.max,
  )
}

private val LazyListState.isBottom: Boolean
  get() {
    val layoutInfo = layoutInfo
    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return true

    return lastVisibleItem.index == layoutInfo.totalItemsCount - 1 &&
      lastVisibleItem.offset + lastVisibleItem.size <= layoutInfo.viewportEndOffset
  }
