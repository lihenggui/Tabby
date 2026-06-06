package com.github.kr328.clash.log.ui

import com.github.kr328.clash.log.model.LogFile

internal fun toLogListItems(
  files: List<LogFile>,
  formatCreated: (Long) -> String,
): List<LogListItem> {
  return files.map { file ->
    LogListItem(fileName = file.fileName, createdText = formatCreated(file.created))
  }
}

internal fun LogListItem.toLogFile(): LogFile? {
  return LogFile.parse(fileName)
}
