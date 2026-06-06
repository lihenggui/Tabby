package com.github.kr328.clash.log.util

import com.github.kr328.clash.core.model.LogMessage
import kotlin.test.Test
import kotlin.test.assertEquals

class LogExportFormatterTest {
  @Test
  fun formatsHeader() {
    assertEquals("# Capture on 2026-06-06 10:00", LogExportFormatter.header("2026-06-06 10:00"))
  }

  @Test
  fun formatsMessageLineWithPadding() {
    assertEquals(
      "    10:00:00    Info: connected",
      LogExportFormatter.message(
        "10:00:00",
        LogMessage(LogMessage.Level.Info, "connected", 1234),
      ),
    )
  }

  @Test
  fun formatsMessageLineWithoutTruncatingLongFields() {
    assertEquals(
      "10:00:00.123 Warning: proxy: changed",
      LogExportFormatter.message(
        "10:00:00.123",
        LogMessage(LogMessage.Level.Warning, "proxy: changed", 1234),
      ),
    )
  }

  @Test
  fun exportWriterWritesHeaderAndMessagesWithFormattedTimes() {
    val output = StringBuilder()
    val progress = mutableListOf<Int>()
    val writer =
      LogcatExportWriter(
        output = output,
        formatHeaderTime = { time -> "created:$time" },
        formatMessageTime = { time -> "time:$time" },
      )

    writer.writeLogFile(
      created = 1000,
      messages =
        listOf(
          LogMessage(LogMessage.Level.Debug, "debug message", 2000),
          LogMessage(LogMessage.Level.Info, "info message", 3000),
        ),
      onProgress = progress::add,
    )

    assertEquals(
      "# Capture on created:1000\n" +
        "   time:2000   Debug: debug message\n" +
        "   time:3000    Info: info message\n",
      output.toString(),
    )
    assertEquals(listOf(1, 2), progress)
  }
}
