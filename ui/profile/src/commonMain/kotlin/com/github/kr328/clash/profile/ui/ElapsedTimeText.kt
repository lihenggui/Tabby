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
  return when (token) {
    ElapsedTimeTextToken.DaysAgo -> stringResource(SharedRes.string.format_days_ago, value)
    ElapsedTimeTextToken.HoursAgo -> stringResource(SharedRes.string.format_hours_ago, value)
    ElapsedTimeTextToken.MinutesAgo -> stringResource(SharedRes.string.format_minutes_ago, value)
    ElapsedTimeTextToken.Recently -> stringResource(SharedRes.string.recently)
  }
}
