package com.github.kr328.clash.log.util

import com.github.kr328.clash.core.model.LogMessage

internal object LogExportFormatter {
  fun header(createdText: String): String {
    return "# Capture on $createdText"
  }

  fun message(timeText: String, message: LogMessage): String {
    return "${timeText.padStart(TIME_WIDTH)} ${message.level.name.padStart(LEVEL_WIDTH)}: ${message.message}"
  }

  private const val TIME_WIDTH = 12
  private const val LEVEL_WIDTH = 7
}
