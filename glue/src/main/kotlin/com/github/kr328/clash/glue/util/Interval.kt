package com.github.kr328.clash.glue.util

import android.content.Context
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.util.TabbyElapsedIntervalText
import com.github.kr328.clash.common.util.tabbyElapsedIntervalText

fun Long.elapsedIntervalString(context: Context): String {
  return when (val text = tabbyElapsedIntervalText(this)) {
    is TabbyElapsedIntervalText.Days ->
      context.getString(CommonR.string.format_days_ago, text.count)
    is TabbyElapsedIntervalText.Hours ->
      context.getString(CommonR.string.format_hours_ago, text.count)
    is TabbyElapsedIntervalText.Minutes ->
      context.getString(CommonR.string.format_minutes_ago, text.count)
    TabbyElapsedIntervalText.Recently -> context.getString(CommonR.string.recently)
  }
}
