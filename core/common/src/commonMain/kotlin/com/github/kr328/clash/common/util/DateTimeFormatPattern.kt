package com.github.kr328.clash.common.util

const val TABBY_DATE_FORMAT_PATTERN = "yyyy-MM-dd"
const val TABBY_TIME_FORMAT_PATTERN = "HH:mm:ss.SSS"
const val TABBY_DATE_TIME_FORMAT_PATTERN = "$TABBY_DATE_FORMAT_PATTERN $TABBY_TIME_FORMAT_PATTERN"
const val TABBY_DATE_TIME_SECONDS_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss"

fun tabbyDateTimeFormatPattern(
  includeDate: Boolean = true,
  includeTime: Boolean = true,
): String? {
  return when {
    includeDate && includeTime -> TABBY_DATE_TIME_FORMAT_PATTERN
    includeDate -> TABBY_DATE_FORMAT_PATTERN
    includeTime -> TABBY_TIME_FORMAT_PATTERN
    else -> null
  }
}
