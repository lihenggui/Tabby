package com.github.kr328.clash.home.ui

import com.github.kr328.clash.core.model.Traffic
import kotlin.math.roundToLong

internal fun homeTrafficTotalText(traffic: Traffic): String {
  return formatHomeBinaryBytes(traffic.homeTotalApproxBytes())
}

private fun Traffic.homeTotalApproxBytes(): Long {
  val upload = normalizeHomeTrafficScaledToCentiBytes(uploadScaled)
  val download = normalizeHomeTrafficScaledToCentiBytes(downloadScaled)

  return centiBytesToApproxBytes(upload.saturatingPlus(download))
}

private fun normalizeHomeTrafficScaledToCentiBytes(scaled: Long): Long {
  return if (scaled <= 1024L) scaled * 100 else scaled
}

private fun centiBytesToApproxBytes(centiBytes: Long): Long = centiBytes / 100

private fun Long.saturatingPlus(other: Long): Long {
  return if (Long.MAX_VALUE - this < other) Long.MAX_VALUE else this + other
}

private fun formatHomeBinaryBytes(bytes: Long): String {
  val unit = homeBinaryUnits.lastOrNull { bytes >= it.bytes } ?: homeBinaryUnits.first()
  val value = bytes.toDouble() / unit.bytes.toDouble()

  return "${formatHomeBinaryValue(value, unit)} ${unit.label}"
}

private fun formatHomeBinaryValue(value: Double, unit: HomeBinaryUnit): String {
  if (unit.bytes == 1L) return value.roundToLong().toString()

  val roundedTenths = (value * 10).roundToLong()
  val whole = roundedTenths / 10
  val tenth = roundedTenths % 10

  return if (tenth == 0L) whole.toString() else "$whole.$tenth"
}

private data class HomeBinaryUnit(val label: String, val bytes: Long)

private val homeBinaryUnits =
  listOf(
    HomeBinaryUnit("B", 1L),
    HomeBinaryUnit("KiB", 1L shl 10),
    HomeBinaryUnit("MiB", 1L shl 20),
    HomeBinaryUnit("GiB", 1L shl 30),
    HomeBinaryUnit("TiB", 1L shl 40),
    HomeBinaryUnit("PiB", 1L shl 50),
    HomeBinaryUnit("EiB", 1L shl 60),
  )
