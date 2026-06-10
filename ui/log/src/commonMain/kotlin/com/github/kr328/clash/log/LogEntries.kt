package com.github.kr328.clash.log

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.github.kr328.clash.ui.nav.TabbyNavDisplay
import com.github.kr328.clash.ui.nav.addIfNotLast

fun EntryProviderScope<NavKey>.logsEntries(rootContent: @Composable () -> Unit) {
  entry<LogRoute.Root> { rootContent() }
}

@Composable
fun LogRouteContent(
  logcatRunning: Boolean,
  logsContent:
    @Composable
    (onStartLogcat: () -> Unit, onOpenFile: (fileName: String) -> Unit) -> Unit,
  logcatContent:
    @Composable
    (
      fileName: String?,
      onOpenLogs: () -> Unit,
      onInvalidFile: () -> Unit,
      onClose: () -> Unit,
    ) -> Unit,
) {
  val backStack = remember {
    if (logcatRunning) {
      mutableStateListOf<NavKey>(LogRoute.Logcat())
    } else {
      mutableStateListOf<NavKey>(LogRoute.Logs)
    }
  }

  TabbyNavDisplay(
    backStack = backStack,
    entryProvider =
      entryProvider<NavKey> {
        entry<LogRoute.Logs> {
          logsContent(
            {
              backStack.removeLastOrNull()
              backStack.addIfNotLast(LogRoute.Logcat())
            },
            { fileName -> backStack.addIfNotLast(LogRoute.Logcat(fileName)) },
          )
        }
        entry<LogRoute.Logcat> { key ->
          logcatContent(
            key.fileName,
            {
              backStack.removeLastOrNull()
              backStack.addIfNotLast(LogRoute.Logs)
            },
            { backStack.removeLastOrNull() },
            { backStack.removeLastOrNull() },
          )
        }
      },
  )
}
