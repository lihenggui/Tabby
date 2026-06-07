package com.github.kr328.clash.log.ui

import com.github.kr328.clash.log.model.LogFile
import kotlin.time.Instant

fun logFileCreatedText(created: Long): String = Instant.fromEpochMilliseconds(created).toString()

internal fun toLogListItems(
  files: List<LogFile>,
  formatCreated: (Long) -> String,
): List<LogListItem> {
  return files.map { file ->
    LogListItem(fileName = file.fileName, createdText = formatCreated(file.created))
  }
}

internal sealed interface LogListItemOpenAction {
  data class OpenFile(val file: LogFile) : LogListItemOpenAction

  object Ignore : LogListItemOpenAction
}

internal fun logListItemOpenAction(item: LogListItem): LogListItemOpenAction {
  return item.toLogFile()?.let(LogListItemOpenAction::OpenFile) ?: LogListItemOpenAction.Ignore
}

internal fun LogListItem.toLogFile(): LogFile? {
  return LogFile.parse(fileName)
}
