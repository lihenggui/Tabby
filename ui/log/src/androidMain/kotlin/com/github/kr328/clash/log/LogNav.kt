package com.github.kr328.clash.log

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.log.ui.LogcatScreen
import com.github.kr328.clash.log.ui.LogsScreen

fun EntryProviderScope<NavKey>.logsEntries() {
  logsEntries {
    LogRouteContent(
      logcatRunning = LogcatService.running.value,
      logsContent = { onStartLogcat, onOpenFile ->
        LogsScreen(
          onStartLogcat = onStartLogcat,
          onOpenFile = { file -> onOpenFile(file.fileName) },
        )
      },
      logcatContent = { fileName, onOpenLogs, onInvalidFile, onClose ->
        LogcatScreen(
          fileName = fileName,
          onOpenLogs = onOpenLogs,
          onInvalidFile = onInvalidFile,
          onClose = onClose,
        )
      },
    )
  }
}
