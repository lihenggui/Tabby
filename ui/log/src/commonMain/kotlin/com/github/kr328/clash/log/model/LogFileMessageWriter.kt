package com.github.kr328.clash.log.model

import com.github.kr328.clash.core.model.LogMessage

internal class LogFileMessageWriter(private val output: Appendable) {
  fun appendMessage(message: LogMessage) {
    output.appendLine(LogFileMessageCodec.encode(message))
  }
}
