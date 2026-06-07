package com.github.kr328.clash.log.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.kr328.clash.log.model.LogFile
import com.github.kr328.clash.log.model.LogFileRepository
import kotlinx.coroutines.launch

@Composable
fun LogFileRepositoryLogsRouteContent(
  logFileRepository: LogFileRepository,
  onStartLogcat: () -> Unit,
  onOpenFile: (String) -> Unit,
  modifier: Modifier = Modifier,
  formatCreated: (Long) -> String = ::logFileCreatedText,
  onActionError: (Throwable) -> Unit = {},
) {
  val scope = rememberCoroutineScope()
  var logs by remember(logFileRepository) { mutableStateOf(emptyList<LogFile>()) }

  suspend fun refreshLogs() {
    logs = logFileRepository.queryLogFiles()
  }

  LaunchedEffect(logFileRepository) { runCatching { refreshLogs() }.onFailure(onActionError) }

  LogsRouteContent(
    modifier = modifier,
    logs = logs,
    formatCreated = formatCreated,
    showDeleteAllAction = logs.isNotEmpty(),
    onDeleteAll = {
      scope.launch {
        runCatching {
            logFileRepository.deleteAllLogFiles()
            refreshLogs()
          }
          .onFailure(onActionError)
      }
    },
    onStartLogcat = onStartLogcat,
    onOpenFile = { file: LogFile -> onOpenFile(file.fileName) },
  )
}
