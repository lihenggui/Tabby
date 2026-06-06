package com.github.kr328.clash.crash.ui

internal fun formatAppCrashLog(lines: List<String>): String {
  return lines.filterNot { it.startsWith("------") }.joinToString("\n").trim()
}
