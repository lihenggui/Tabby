package com.github.kr328.clash.log.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LogFileTest {
  @Test
  fun parseLogFileName() {
    assertEquals(LogFile("clash-1234.log", 1234), LogFile.parse("clash-1234.log"))
  }

  @Test
  fun parseRejectsInvalidFileName() {
    assertNull(LogFile.parse("clash.log"))
    assertNull(LogFile.parse("clash--1234.log"))
    assertNull(LogFile.parse("clash-1234xlog"))
    assertNull(LogFile.parse("clash-1234.txt"))
    assertNull(LogFile.parse("prefix-clash-1234.log"))
    assertNull(LogFile.parse("clash-9223372036854775808.log"))
  }

  @Test
  fun createLogFileNameFromCreatedTime() {
    assertEquals(LogFile("clash-5678.log", 5678), LogFile.fromCreatedTime(5678))
  }
}
