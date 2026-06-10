package com.github.kr328.clash.common.util

import kotlin.time.Duration.Companion.milliseconds

sealed interface TabbyElapsedIntervalText {
  data class Days(val count: Long) : TabbyElapsedIntervalText

  data class Hours(val count: Long) : TabbyElapsedIntervalText

  data class Minutes(val count: Long) : TabbyElapsedIntervalText

  data object Recently : TabbyElapsedIntervalText
}

fun tabbyElapsedIntervalText(elapsedMillis: Long): TabbyElapsedIntervalText {
  val duration = elapsedMillis.milliseconds
  val days = duration.inWholeDays
  val hours = duration.inWholeHours
  val minutes = duration.inWholeMinutes

  return when {
    days > 0 -> TabbyElapsedIntervalText.Days(days)
    hours > 0 -> TabbyElapsedIntervalText.Hours(hours)
    minutes > 0 -> TabbyElapsedIntervalText.Minutes(minutes)
    else -> TabbyElapsedIntervalText.Recently
  }
}
