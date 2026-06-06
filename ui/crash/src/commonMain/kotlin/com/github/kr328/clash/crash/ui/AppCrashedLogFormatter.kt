package com.github.kr328.clash.crash.ui

internal sealed interface AppCrashLogDumpResult {
  data class UseLog(val log: String) : AppCrashLogDumpResult

  data class LogcatFailed(val message: String) : AppCrashLogDumpResult
}

internal fun formatAppCrashLog(lines: List<String>): String {
  return lines.filterNot { it.startsWith("------") }.joinToString("\n").trim()
}

internal fun appCrashLogDumpResult(exitCode: Int, log: String): AppCrashLogDumpResult {
  return if (exitCode == 0) {
    AppCrashLogDumpResult.UseLog(log)
  } else {
    AppCrashLogDumpResult.LogcatFailed("logcat exited with code $exitCode: $log")
  }
}
