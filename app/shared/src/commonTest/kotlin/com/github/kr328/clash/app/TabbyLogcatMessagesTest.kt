package com.github.kr328.clash.app

import com.github.kr328.clash.core.model.LogMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TabbyLogcatMessagesTest {
  @Test
  fun appendMessageKeepsMessagesWithinCapacity() {
    val first = logMessage("first")
    val second = logMessage("second")
    val third = logMessage("third")

    assertEquals(
      listOf(second, third),
      tabbyLogcatMessagesAfterAppend(listOf(first, second), third, capacity = 2),
    )
  }

  @Test
  fun appendMessagePreservesMessagesBelowCapacity() {
    val first = logMessage("first")
    val second = logMessage("second")

    assertEquals(
      listOf(first, second),
      tabbyLogcatMessagesAfterAppend(listOf(first), second, capacity = 2),
    )
  }

  @Test
  fun appendMessageRejectsInvalidCapacity() {
    assertFailsWith<IllegalArgumentException> {
      tabbyLogcatMessagesAfterAppend(emptyList(), logMessage("message"), capacity = 0)
    }
  }

  private fun logMessage(message: String): LogMessage {
    return LogMessage(
      level = LogMessage.Level.Info,
      message = message,
      time = message.length.toLong(),
    )
  }
}
