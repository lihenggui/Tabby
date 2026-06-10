package com.github.kr328.clash.common.util

import kotlin.test.Test
import kotlin.test.assertEquals

class ElapsedIntervalTextTest {
  @Test
  fun mapsSubMinuteAndNegativeIntervalsToRecently() {
    assertEquals(TabbyElapsedIntervalText.Recently, tabbyElapsedIntervalText(-1))
    assertEquals(TabbyElapsedIntervalText.Recently, tabbyElapsedIntervalText(0))
    assertEquals(TabbyElapsedIntervalText.Recently, tabbyElapsedIntervalText(59_999))
  }

  @Test
  fun mapsElapsedMillisToMinuteHourAndDayTokens() {
    assertEquals(TabbyElapsedIntervalText.Minutes(1), tabbyElapsedIntervalText(60_000))
    assertEquals(TabbyElapsedIntervalText.Minutes(59), tabbyElapsedIntervalText(3_599_999))
    assertEquals(TabbyElapsedIntervalText.Hours(1), tabbyElapsedIntervalText(3_600_000))
    assertEquals(TabbyElapsedIntervalText.Hours(23), tabbyElapsedIntervalText(86_399_999))
    assertEquals(TabbyElapsedIntervalText.Days(1), tabbyElapsedIntervalText(86_400_000))
  }
}
