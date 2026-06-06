package com.github.kr328.clash.log.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LogFileSelectionTest {
  @Test
  fun createsInitialLogFilesState() {
    assertEquals(emptyList(), logFilesInitialState())
  }

  @Test
  fun logFilesAreSelectedInInputOrder() {
    val files = selectLogFiles(listOf("clash-20.log", "clash-10.log", "clash-30.log"))

    assertEquals(
      listOf(
        LogFile(fileName = "clash-20.log", created = 20),
        LogFile(fileName = "clash-10.log", created = 10),
        LogFile(fileName = "clash-30.log", created = 30),
      ),
      files,
    )
  }

  @Test
  fun invalidLogFileNamesAreSkipped() {
    val files =
      selectLogFiles(
        listOf(
          "clash.log",
          "clash-1.log",
          "clash-9223372036854775808.log",
          "clash-2.txt",
          "clash-3.log",
        )
      )

    assertEquals(
      listOf(
        LogFile(fileName = "clash-1.log", created = 1),
        LogFile(fileName = "clash-3.log", created = 3),
      ),
      files,
    )
  }
}
