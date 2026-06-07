package com.github.kr328.clash.profile.ui

import androidx.compose.runtime.Composable
import kotlin.time.Duration.Companion.milliseconds
import org.jetbrains.compose.resources.stringResource
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.format_days_ago
import tabby.ui.shared.generated.resources.format_hours_ago
import tabby.ui.shared.generated.resources.format_minutes_ago
import tabby.ui.shared.generated.resources.recently

internal enum class ElapsedTimeTextToken {
  Recently,
  MinutesAgo,
  HoursAgo,
  DaysAgo,
}

internal fun <T> elapsedTimeTextPlatformToken(
  token: ElapsedTimeTextToken,
  recently: T,
  minutesAgo: T,
  hoursAgo: T,
  daysAgo: T,
): T {
  return when (token) {
    ElapsedTimeTextToken.Recently -> recently
    ElapsedTimeTextToken.MinutesAgo -> minutesAgo
    ElapsedTimeTextToken.HoursAgo -> hoursAgo
    ElapsedTimeTextToken.DaysAgo -> daysAgo
  }
}

internal fun elapsedTimeTextUsesValue(token: ElapsedTimeTextToken): Boolean {
  return when (token) {
    ElapsedTimeTextToken.Recently -> false
    ElapsedTimeTextToken.MinutesAgo,
    ElapsedTimeTextToken.HoursAgo,
    ElapsedTimeTextToken.DaysAgo -> true
  }
}

internal data class ElapsedTimeText(
  val token: ElapsedTimeTextToken,
  val value: Long = 0,
)

internal fun elapsedTimeText(elapsedMillis: Long): ElapsedTimeText {
  val duration = elapsedMillis.coerceAtLeast(0).milliseconds
  val day = duration.inWholeDays
  val hour = duration.inWholeHours
  val minute = duration.inWholeMinutes

  return when {
    day > 0 -> ElapsedTimeText(token = ElapsedTimeTextToken.DaysAgo, value = day)
    hour > 0 -> ElapsedTimeText(token = ElapsedTimeTextToken.HoursAgo, value = hour)
    minute > 0 -> ElapsedTimeText(token = ElapsedTimeTextToken.MinutesAgo, value = minute)
    else -> ElapsedTimeText(token = ElapsedTimeTextToken.Recently)
  }
}

@Composable
internal fun elapsedTimeTextString(elapsedMillis: Long): String {
  return elapsedTimeText(elapsedMillis).stringResource()
}

@Composable
private fun ElapsedTimeText.stringResource(): String {
  val resource =
    elapsedTimeTextPlatformToken(
      token = token,
      recently = SharedRes.string.recently,
      minutesAgo = SharedRes.string.format_minutes_ago,
      hoursAgo = SharedRes.string.format_hours_ago,
      daysAgo = SharedRes.string.format_days_ago,
    )

  return if (elapsedTimeTextUsesValue(token)) {
    stringResource(resource, value)
  } else {
    stringResource(resource)
  }
}
