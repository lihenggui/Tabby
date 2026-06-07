package com.github.kr328.clash.app

import com.github.kr328.clash.core.model.LogMessage

internal const val TABBY_LOGCAT_MESSAGES_CAPACITY = 128

internal fun tabbyLogcatMessagesAfterAppend(
  messages: List<LogMessage>,
  message: LogMessage,
  capacity: Int = TABBY_LOGCAT_MESSAGES_CAPACITY,
): List<LogMessage> {
  require(capacity > 0) { "capacity must be positive" }

  return (messages + message).takeLast(capacity)
}
