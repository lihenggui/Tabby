package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile

internal fun Profile.toProfileListItem(
  currentTime: Long,
  formatType: (Profile.Type) -> String,
  formatUnsavedType: (String) -> String,
  formatBytes: (Long) -> String,
  formatExpire: (Long) -> String,
  formatElapsedMillis: (Long) -> String,
): ProfileListItem {
  val baseTypeText = formatType(type)
  val showTraffic = showsTrafficUsage()
  val usedTraffic = download + upload

  return ProfileListItem(
    profile = this,
    typeText = if (pending) formatUnsavedType(baseTypeText) else baseTypeText,
    usageText =
      if (showTraffic) {
        "${formatBytes(usedTraffic)} / ${formatBytes(total)}"
      } else {
        null
      },
    expireText = expire.takeIf { it != 0L }?.let(formatExpire),
    updatedAtText = formatElapsedMillis(currentTime - updatedAt),
    trafficProgress = if (showTraffic) trafficProgress(usedTraffic = usedTraffic) else 0,
  )
}

private fun Profile.showsTrafficUsage(): Boolean {
  return download >= MIN_DOWNLOAD_BYTES_FOR_USAGE && total > MIN_TOTAL_BYTES_FOR_USAGE
}

private fun Profile.trafficProgress(usedTraffic: Long): Int {
  return (usedTraffic.toDouble() / total.toDouble() * TRAFFIC_PROGRESS_SCALE)
    .toInt()
    .coerceIn(0, TRAFFIC_PROGRESS_SCALE)
}

private const val MIN_DOWNLOAD_BYTES_FOR_USAGE = 2L
private const val MIN_TOTAL_BYTES_FOR_USAGE = 1L
private const val TRAFFIC_PROGRESS_SCALE = 1000
