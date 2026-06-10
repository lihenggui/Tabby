package com.github.kr328.clash.common.service

import kotlin.test.Test
import kotlin.test.assertEquals

class TimeZoneOffsetTest {
  @Test
  fun convertsRawOffsetMillisToSeconds() {
    assertEquals(28_800, tabbyTimeZoneRawOffsetSeconds(28_800_000))
    assertEquals(-18_000, tabbyTimeZoneRawOffsetSeconds(-18_000_000))
    assertEquals(0, tabbyTimeZoneRawOffsetSeconds(0))
  }

  @Test
  fun preservesIntegerDivisionTowardZero() {
    assertEquals(1, tabbyTimeZoneRawOffsetSeconds(1_999))
    assertEquals(-1, tabbyTimeZoneRawOffsetSeconds(-1_999))
  }
}
