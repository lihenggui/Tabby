package com.github.kr328.clash.log.model

import com.github.kr328.clash.core.model.LogMessage

object LogFileMessageCodec {
  fun encode(message: LogMessage): String {
    return "${message.time}:${message.level.name}:${message.message}"
  }

  fun decode(line: String, fallbackTime: Long): LogMessage? {
    val value = line.trim()

    if (value.startsWith("#")) {
      return null
    }

    val parts = value.split(":", limit = 3)
    val parsedTime = parts[0].toLongOrNull()
    val time = parsedTime ?: fallbackTime

    return if (parsedTime != null && parts.size >= 3) {
      LogMessage(
        time = time,
        level = LogMessage.Level.valueOf(parts[1]),
        message = parts[2],
      )
    } else {
      LogMessage(time = time, level = LogMessage.Level.Warning, message = value)
    }
  }

  fun decodeLines(lines: Sequence<String>): List<LogMessage> {
    var lastTime = 0L
    return lines
      .mapNotNull { line ->
        val logMessage = decode(line, lastTime)

        if (logMessage != null) {
          lastTime = logMessage.time
        }

        logMessage
      }
      .toList()
  }
}
