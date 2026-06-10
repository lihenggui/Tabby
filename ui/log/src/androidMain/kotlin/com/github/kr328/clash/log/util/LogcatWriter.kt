package com.github.kr328.clash.log.util

import android.content.Context
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.glue.util.logsDir
import com.github.kr328.clash.log.model.LogFile
import com.github.kr328.clash.log.model.LogFileMessageWriter
import java.io.BufferedWriter

internal class LogcatWriter(
  context: Context,
  file: LogFile = LogFile.fromCreatedTime(System.currentTimeMillis()),
  private val writer: BufferedWriter = context.logsDir.resolve(file.fileName).bufferedWriter(),
) : AutoCloseable by writer {
  private val messageWriter = LogFileMessageWriter(writer)

  fun appendMessage(message: LogMessage) {
    messageWriter.appendMessage(message)
  }
}
