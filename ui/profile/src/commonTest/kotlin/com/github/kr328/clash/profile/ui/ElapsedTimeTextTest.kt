package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ElapsedTimeTextTest {
  @Test
  fun elapsedTimeTextPlatformTokenMapsTokensToPlatformResources() {
    assertEquals(
      "recently",
      elapsedTimeTextPlatformToken(
        token = ElapsedTimeTextToken.Recently,
        recently = "recently",
        minutesAgo = "minutes",
        hoursAgo = "hours",
        daysAgo = "days",
      ),
    )
    assertEquals(
      "minutes",
      elapsedTimeTextPlatformToken(
        token = ElapsedTimeTextToken.MinutesAgo,
        recently = "recently",
        minutesAgo = "minutes",
        hoursAgo = "hours",
        daysAgo = "days",
      ),
    )
    assertEquals(
      "hours",
      elapsedTimeTextPlatformToken(
        token = ElapsedTimeTextToken.HoursAgo,
        recently = "recently",
        minutesAgo = "minutes",
        hoursAgo = "hours",
        daysAgo = "days",
      ),
    )
    assertEquals(
      "days",
      elapsedTimeTextPlatformToken(
        token = ElapsedTimeTextToken.DaysAgo,
        recently = "recently",
        minutesAgo = "minutes",
        hoursAgo = "hours",
        daysAgo = "days",
      ),
    )
  }

  @Test
  fun elapsedTimeTextUsesValueOnlyForFormattedDurations() {
    assertEquals(false, elapsedTimeTextUsesValue(ElapsedTimeTextToken.Recently))
    assertEquals(true, elapsedTimeTextUsesValue(ElapsedTimeTextToken.MinutesAgo))
    assertEquals(true, elapsedTimeTextUsesValue(ElapsedTimeTextToken.HoursAgo))
    assertEquals(true, elapsedTimeTextUsesValue(ElapsedTimeTextToken.DaysAgo))
  }

  @Test
  fun negativeElapsedTimeIsRecently() {
    assertEquals(
      ElapsedTimeText(token = ElapsedTimeTextToken.Recently),
      elapsedTimeText((-1).seconds.inWholeMilliseconds),
    )
  }

  @Test
  fun subMinuteElapsedTimeIsRecently() {
    assertEquals(
      ElapsedTimeText(token = ElapsedTimeTextToken.Recently),
      elapsedTimeText(59.seconds.inWholeMilliseconds),
    )
  }

  @Test
  fun minuteElapsedTimeUsesWholeMinutes() {
    assertEquals(
      ElapsedTimeText(token = ElapsedTimeTextToken.MinutesAgo, value = 2),
      elapsedTimeText(2.minutes.plus(30.seconds).inWholeMilliseconds),
    )
  }

  @Test
  fun hourElapsedTimeUsesWholeHours() {
    assertEquals(
      ElapsedTimeText(token = ElapsedTimeTextToken.HoursAgo, value = 3),
      elapsedTimeText(3.hours.plus(45.minutes).inWholeMilliseconds),
    )
  }

  @Test
  fun dayElapsedTimeUsesWholeDays() {
    assertEquals(
      ElapsedTimeText(token = ElapsedTimeTextToken.DaysAgo, value = 4),
      elapsedTimeText(4.days.plus(23.hours).inWholeMilliseconds),
    )
  }
}
