package com.github.kr328.clash.log.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.kr328.clash.log.model.LogFile

@Composable
fun LogsRouteContent(
  logs: List<LogFile>,
  formatCreated: (Long) -> String,
  onDeleteAll: () -> Unit,
  onStartLogcat: () -> Unit,
  onOpenFile: (LogFile) -> Unit,
  modifier: Modifier = Modifier,
  showDeleteAllAction: Boolean = true,
) {
  LogsContent(
    modifier = modifier,
    logs = toLogListItems(files = logs, formatCreated = formatCreated),
    showDeleteAllAction = showDeleteAllAction,
    onDeleteAll = onDeleteAll,
    onStartLogcat = onStartLogcat,
    onOpenFile = { item ->
      when (val action = logListItemOpenAction(item)) {
        is LogListItemOpenAction.OpenFile -> onOpenFile(action.file)
        LogListItemOpenAction.Ignore -> Unit
      }
    },
  )
}
