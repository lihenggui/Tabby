package com.github.kr328.clash.profile.ui

import android.content.Context
import com.github.kr328.clash.common.R as CommonR

internal fun elapsedTimeTextString(context: Context, elapsedMillis: Long): String {
  return elapsedTimeText(elapsedMillis).androidString(context)
}

private fun ElapsedTimeText.androidString(context: Context): String {
  val resource =
    elapsedTimeTextResourceToken(
      token = token,
      recently = CommonR.string.recently,
      minutesAgo = CommonR.string.format_minutes_ago,
      hoursAgo = CommonR.string.format_hours_ago,
      daysAgo = CommonR.string.format_days_ago,
    )

  return if (elapsedTimeTextUsesValue(token)) {
    context.getString(resource, value)
  } else {
    context.getString(resource)
  }
}
