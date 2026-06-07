package com.github.kr328.clash.profile.ui

import kotlin.math.roundToLong

internal fun profileBinaryBytesText(bytes: Long): String {
  val units = arrayOf("B", "KiB", "MiB", "GiB", "TiB", "PiB")
  var value = bytes.coerceAtLeast(0).toDouble()
  var unitIndex = 0

  while (value >= PROFILE_BINARY_UNIT_SIZE && unitIndex < units.lastIndex) {
    value /= PROFILE_BINARY_UNIT_SIZE
    unitIndex += 1
  }

  return if (unitIndex == 0) {
    "${value.toLong()} ${units[unitIndex]}"
  } else {
    "${value.profileOneDecimalString()} ${units[unitIndex]}"
  }
}

private fun Double.profileOneDecimalString(): String {
  val rounded = (this * 10.0).roundToLong()
  val whole = rounded / 10L
  val fraction = rounded % 10L

  return if (fraction == 0L) {
    whole.toString()
  } else {
    "$whole.$fraction"
  }
}

private const val PROFILE_BINARY_UNIT_SIZE = 1024.0
