package com.github.kr328.clash.profile.ui

import androidx.compose.runtime.Composable
import com.github.kr328.clash.common.util.TabbyElapsedIntervalText
import com.github.kr328.clash.common.util.tabbyElapsedIntervalText
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

internal fun <T> elapsedTimeTextResourceToken(
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
  return when (val text = tabbyElapsedIntervalText(elapsedMillis)) {
    is TabbyElapsedIntervalText.Days ->
      ElapsedTimeText(token = ElapsedTimeTextToken.DaysAgo, value = text.count)
    is TabbyElapsedIntervalText.Hours ->
      ElapsedTimeText(token = ElapsedTimeTextToken.HoursAgo, value = text.count)
    is TabbyElapsedIntervalText.Minutes ->
      ElapsedTimeText(token = ElapsedTimeTextToken.MinutesAgo, value = text.count)
    TabbyElapsedIntervalText.Recently -> ElapsedTimeText(token = ElapsedTimeTextToken.Recently)
  }
}

@Composable
internal fun elapsedTimeTextString(elapsedMillis: Long): String {
  return elapsedTimeText(elapsedMillis).stringResource()
}

@Composable
private fun ElapsedTimeText.stringResource(): String {
  val resource =
    elapsedTimeTextResourceToken(
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
