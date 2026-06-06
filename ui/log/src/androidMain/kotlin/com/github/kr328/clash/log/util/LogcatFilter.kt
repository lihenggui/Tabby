package com.github.kr328.clash.log.util

import android.content.Context
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.glue.util.format
import java.io.BufferedWriter
import java.io.Writer
import java.util.Date

internal class LogcatFilter(output: Writer, private val context: Context) : BufferedWriter(output) {
  fun writeHeader(created: Long) {
    appendLine(LogExportFormatter.header(Date(created).format(context)))
  }

  fun writeMessage(message: LogMessage) {
    appendLine(
      LogExportFormatter.message(Date(message.time).format(context, includeDate = false), message)
    )
  }
}
