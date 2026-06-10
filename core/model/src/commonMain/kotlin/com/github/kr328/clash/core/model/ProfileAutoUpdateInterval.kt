package com.github.kr328.clash.core.model

import kotlin.time.Duration.Companion.minutes

private const val MINIMUM_PROFILE_AUTO_UPDATE_INTERVAL_MINUTES = 15L
private const val MINIMUM_PROFILE_AUTO_UPDATE_INTERVAL_MILLIS =
  MINIMUM_PROFILE_AUTO_UPDATE_INTERVAL_MINUTES * 60 * 1000L

fun isProfileAutoUpdateIntervalMinutesInput(value: String): Boolean {
  return value.isEmpty() ||
    (value.toLongOrNull() ?: 0) >= MINIMUM_PROFILE_AUTO_UPDATE_INTERVAL_MINUTES
}

fun profileAutoUpdateIntervalMillisFromMinutesInput(value: String): Long? {
  if (!isProfileAutoUpdateIntervalMinutesInput(value)) return null

  val minutes = value.toLongOrNull() ?: 0
  return minutes.minutes.inWholeMilliseconds
}

fun isValidProfileAutoUpdateIntervalMillis(interval: Long): Boolean {
  return interval == 0L || interval >= MINIMUM_PROFILE_AUTO_UPDATE_INTERVAL_MILLIS
}

fun profileSupportsAutoUpdateSchedule(type: Profile.Type): Boolean {
  return type != Profile.Type.File
}

fun profileAutoUpdateScheduleDelayMillis(
  interval: Long,
  currentTimeMillis: Long,
  lastModifiedMillis: Long,
): Long? {
  if (interval < MINIMUM_PROFILE_AUTO_UPDATE_INTERVAL_MILLIS) return null
  if (lastModifiedMillis < 0) return null

  return (interval - (currentTimeMillis - lastModifiedMillis)).coerceAtLeast(0)
}
