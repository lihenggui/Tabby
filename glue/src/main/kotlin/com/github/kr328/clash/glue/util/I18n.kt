package com.github.kr328.clash.glue.util

import android.content.Context
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.compat.preferredLocale
import com.github.kr328.clash.common.util.TABBY_DATE_TIME_SECONDS_FORMAT_PATTERN
import com.github.kr328.clash.common.util.tabbyDateTimeFormatPattern
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.core.model.Provider
import java.text.SimpleDateFormat
import java.util.Date

fun Profile.Type.toString(context: Context): String {
  return when (this) {
    File -> context.getString(CommonR.string.file)
    Url -> context.getString(CommonR.string.url)
    External -> context.getString(CommonR.string.external)
  }
}

fun Provider.type(context: Context): String {
  val type =
    when (type) {
      Proxy -> context.getString(CommonR.string.proxy)
      Rule -> context.getString(CommonR.string.rule)
    }

  val vehicle =
    when (vehicleType) {
      HTTP -> context.getString(CommonR.string.http)
      File -> context.getString(CommonR.string.file)
      Inline -> context.getString(CommonR.string.inline)
      Compatible -> context.getString(CommonR.string.compatible)
    }

  return context.getString(CommonR.string.format_provider_type, type, vehicle)
}

@JvmOverloads
fun Date.format(
  context: Context,
  includeDate: Boolean = true,
  includeTime: Boolean = true,
): String {
  val locale = context.resources.configuration.preferredLocale
  val pattern = tabbyDateTimeFormatPattern(includeDate = includeDate, includeTime = includeTime)

  return pattern?.let { SimpleDateFormat(it, locale).format(this) }.orEmpty()
}

fun Long.toDateStr(): String {
  val simpleDateFormat = SimpleDateFormat(TABBY_DATE_TIME_SECONDS_FORMAT_PATTERN)
  return simpleDateFormat.format(Date(this))
}
