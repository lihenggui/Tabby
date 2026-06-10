package com.github.kr328.clash.crash.ui

internal sealed interface AppCrashLogDumpResult {
  data class UseLog(val log: String) : AppCrashLogDumpResult

  data class LogcatFailed(val message: String) : AppCrashLogDumpResult
}

internal sealed interface AppCrashLogLoadAction {
  data class UseLog(val log: String) : AppCrashLogLoadAction

  data class Fail(val message: String) : AppCrashLogLoadAction
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

internal fun appCrashLogLoadAction(exitCode: Int, log: String): AppCrashLogLoadAction {
  return appCrashLogLoadAction(appCrashLogDumpResult(exitCode, log))
}

internal fun appCrashLogLoadAction(result: AppCrashLogDumpResult): AppCrashLogLoadAction {
  return when (result) {
    is AppCrashLogDumpResult.UseLog -> AppCrashLogLoadAction.UseLog(result.log)
    is AppCrashLogDumpResult.LogcatFailed -> AppCrashLogLoadAction.Fail(result.message)
  }
}

fun appCrashLogLoadFailureMessage(cause: Throwable): String {
  return "Failed to load crash logs: $cause"
}
