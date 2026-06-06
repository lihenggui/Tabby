package com.github.kr328.clash.log.model

import com.github.kr328.clash.core.model.LogMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LogFileMessageCodecTest {
  @Test
  fun encodeMessageLine() {
    val message = LogMessage(LogMessage.Level.Info, "proxy: selected", 1234)

    assertEquals("1234:Info:proxy: selected", LogFileMessageCodec.encode(message))
  }

  @Test
  fun decodeMessageLine() {
    assertEquals(
      LogMessage(LogMessage.Level.Warning, "proxy: changed", 1234),
      LogFileMessageCodec.decode("1234:Warning:proxy: changed", fallbackTime = 0),
    )
  }

  @Test
  fun decodeMalformedLineUsesFallbackTimeAndWarningLevel() {
    assertEquals(
      LogMessage(LogMessage.Level.Warning, "not a structured line", 5678),
      LogFileMessageCodec.decode("not a structured line", fallbackTime = 5678),
    )
  }

  @Test
  fun decodeTrimsLine() {
    assertEquals(
      LogMessage(LogMessage.Level.Info, "connected", 1234),
      LogFileMessageCodec.decode("  1234:Info:connected  ", fallbackTime = 0),
    )
  }

  @Test
  fun decodeSkipsHeaderLine() {
    assertNull(LogFileMessageCodec.decode("# Capture on today", fallbackTime = 1234))
  }

  @Test
  fun decodeLinesUsesPreviousMessageTimeForMalformedLines() {
    assertEquals(
      listOf(
        LogMessage(LogMessage.Level.Info, "started", 1000),
        LogMessage(LogMessage.Level.Warning, "legacy unstructured line", 1000),
        LogMessage(LogMessage.Level.Error, "failed", 2000),
      ),
      LogFileMessageCodec.decodeLines(
        sequenceOf(
          "# Capture on today",
          "1000:Info:started",
          "legacy unstructured line",
          "2000:Error:failed",
        )
      ),
    )
  }

  @Test
  fun decodeLinesUsesZeroFallbackBeforeFirstMessage() {
    assertEquals(
      listOf(LogMessage(LogMessage.Level.Warning, "legacy unstructured line", 0)),
      LogFileMessageCodec.decodeLines(sequenceOf("legacy unstructured line")),
    )
  }
}
