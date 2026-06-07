package com.github.kr328.clash.profile.ui

import android.content.Context
import com.github.kr328.clash.common.R as CommonR

internal fun elapsedTimeTextString(context: Context, elapsedMillis: Long): String {
  return elapsedTimeText(elapsedMillis).androidString(context)
}

private fun ElapsedTimeText.androidString(context: Context): String {
  return when (token) {
    ElapsedTimeTextToken.DaysAgo -> context.getString(CommonR.string.format_days_ago, value)
    ElapsedTimeTextToken.HoursAgo -> context.getString(CommonR.string.format_hours_ago, value)
    ElapsedTimeTextToken.MinutesAgo -> context.getString(CommonR.string.format_minutes_ago, value)
    ElapsedTimeTextToken.Recently -> context.getString(CommonR.string.recently)
  }
}
