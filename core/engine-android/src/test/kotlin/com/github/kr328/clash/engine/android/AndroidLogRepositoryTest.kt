package com.github.kr328.clash.engine.android

import com.github.kr328.clash.core.model.LogMessage
import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidLogRepositoryTest {
  @Test
  fun decodesServiceLogPayload() {
    val log =
      decodeAndroidLogMessage(
        """{"level":"warning","message":"proxy changed","time":1234,"ignored":true}"""
      )

    assertEquals(LogMessage(LogMessage.Level.Warning, "proxy changed", 1234), log)
  }
}
