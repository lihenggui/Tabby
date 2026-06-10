package com.github.kr328.clash.common.remote

const val TABBY_REMOTE_SERVICE_DISCONNECT_CRASH_INTERVAL_MILLIS = 10_000L

fun tabbyRemoteServiceShouldReportCrashOnDisconnect(
  lastDisconnectedAtMillis: Long,
  currentTimeMillis: Long,
  crashIntervalMillis: Long = TABBY_REMOTE_SERVICE_DISCONNECT_CRASH_INTERVAL_MILLIS,
): Boolean {
  return currentTimeMillis - lastDisconnectedAtMillis < crashIntervalMillis
}
