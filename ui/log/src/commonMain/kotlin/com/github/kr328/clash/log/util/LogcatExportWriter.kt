package com.github.kr328.clash.log.util

import com.github.kr328.clash.core.model.LogMessage

internal class LogcatExportWriter(
  private val output: Appendable,
  private val formatHeaderTime: (Long) -> String,
  private val formatMessageTime: (Long) -> String,
) {
  fun writeHeader(created: Long) {
    output.appendLine(LogExportFormatter.header(formatHeaderTime(created)))
  }

  fun writeMessage(message: LogMessage) {
    output.appendLine(LogExportFormatter.message(formatMessageTime(message.time), message))
  }

  fun writeLogFile(
    created: Long,
    messages: List<LogMessage>,
    onProgress: (Int) -> Unit,
  ) {
    writeHeader(created)
    messages.forEachIndexed { index, message ->
      onProgress(index + 1)
      writeMessage(message)
    }
  }
}
