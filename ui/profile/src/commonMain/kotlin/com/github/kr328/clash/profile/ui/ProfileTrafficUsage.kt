package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile

internal fun Profile.showsTrafficUsage(): Boolean {
  return download >= MIN_DOWNLOAD_BYTES_FOR_USAGE && total > MIN_TOTAL_BYTES_FOR_USAGE
}

internal fun Profile.trafficProgress(usedTraffic: Long): Int {
  return (usedTraffic.toDouble() / total.toDouble() * TRAFFIC_PROGRESS_SCALE)
    .toInt()
    .coerceIn(0, TRAFFIC_PROGRESS_SCALE)
}

private const val MIN_DOWNLOAD_BYTES_FOR_USAGE = 2L
private const val MIN_TOTAL_BYTES_FOR_USAGE = 1L
private const val TRAFFIC_PROGRESS_SCALE = 1000
