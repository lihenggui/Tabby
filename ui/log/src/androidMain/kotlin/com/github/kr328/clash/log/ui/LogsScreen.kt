package com.github.kr328.clash.log.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.kr328.clash.glue.util.format
import com.github.kr328.clash.log.model.AndroidLogFileStorage
import com.github.kr328.clash.log.model.LogFileRepository
import java.util.Date

@Composable
internal fun LogsScreen(
  modifier: Modifier = Modifier,
  onStartLogcat: () -> Unit,
  onOpenFile: (String) -> Unit,
) {
  val context = LocalContext.current
  val appContext = context.applicationContext
  val logFileRepository =
    remember(appContext) { LogFileRepository(AndroidLogFileStorage(appContext)) }

  LogFileRepositoryLogsRouteContent(
    logFileRepository = logFileRepository,
    modifier = modifier,
    formatCreated = { created -> Date(created).format(context) },
    onStartLogcat = onStartLogcat,
    onOpenFile = onOpenFile,
  )
}
