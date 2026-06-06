package com.github.kr328.clash.log.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.glue.util.format
import com.github.kr328.clash.log.model.LogFile
import com.github.kr328.clash.log.vm.LogsViewModel
import java.util.Date

@Composable
internal fun LogsScreen(
  modifier: Modifier = Modifier,
  viewModel: LogsViewModel = viewModel(),
  onStartLogcat: () -> Unit,
  onOpenFile: (LogFile) -> Unit,
) {
  val context = LocalContext.current
  val logs by viewModel.logFiles.collectAsStateWithLifecycle()

  LogsContent(
    modifier = modifier,
    logs =
      logs.map { file ->
        LogListItem(fileName = file.fileName, createdText = Date(file.created).format(context))
      },
    onDeleteAll = viewModel::deleteAll,
    onStartLogcat = onStartLogcat,
    onOpenFile = { item -> LogFile.parse(item.fileName)?.let(onOpenFile) },
  )
}
