package com.github.kr328.clash.common.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DateTimeFormatPatternTest {
  @Test
  fun usesDateAndTimePatternWhenBothPartsAreRequested() {
    assertEquals(
      "yyyy-MM-dd HH:mm:ss.SSS",
      tabbyDateTimeFormatPattern(includeDate = true, includeTime = true),
    )
  }

  @Test
  fun exposesDateAndTimeSecondsPatternForCompactTimestamps() {
    assertEquals("yyyy-MM-dd HH:mm:ss", TABBY_DATE_TIME_SECONDS_FORMAT_PATTERN)
  }

  @Test
  fun usesDateOnlyPatternWhenOnlyDateIsRequested() {
    assertEquals(
      "yyyy-MM-dd",
      tabbyDateTimeFormatPattern(includeDate = true, includeTime = false),
    )
  }

  @Test
  fun usesTimeOnlyPatternWhenOnlyTimeIsRequested() {
    assertEquals(
      "HH:mm:ss.SSS",
      tabbyDateTimeFormatPattern(includeDate = false, includeTime = true),
    )
  }

  @Test
  fun returnsNullWhenNoDateTimePartIsRequested() {
    assertNull(tabbyDateTimeFormatPattern(includeDate = false, includeTime = false))
  }
}
