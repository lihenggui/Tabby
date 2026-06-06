package com.github.kr328.clash.log.util

import android.content.Context
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.glue.util.logsDir
import com.github.kr328.clash.log.model.LogFile
import com.github.kr328.clash.log.model.LogFileMessageCodec
import java.io.BufferedReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class LogcatReader(
  context: Context,
  file: LogFile,
  private val reader: BufferedReader = context.logsDir.resolve(file.fileName).bufferedReader(),
) : AutoCloseable by reader {

  suspend fun readAll(): List<LogMessage> =
    withContext(Dispatchers.IO) {
      var lastTime = 0L
      reader
        .lineSequence()
        .mapNotNull { line ->
          val logMessage = LogFileMessageCodec.decode(line, lastTime)

          if (logMessage != null) {
            lastTime = logMessage.time
          }

          logMessage
        }
        .toList()
    }
}
