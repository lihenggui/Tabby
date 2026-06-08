package com.github.kr328.clash.crash.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class AppCrashedLogFormatterTest {
  @Test
  fun separatorLinesAreRemoved() {
    val log =
      formatAppCrashLog(
        listOf(
          "------ beginning of crash",
          "AndroidRuntime: crash",
          "------ end",
          "Tabby: marker",
        )
      )

    assertEquals("AndroidRuntime: crash\nTabby: marker", log)
  }

  @Test
  fun formattedLogIsTrimmed() {
    val log = formatAppCrashLog(listOf("", "  AndroidRuntime: crash  ", ""))

    assertEquals("AndroidRuntime: crash", log)
  }

  @Test
  fun separatorWithLeadingWhitespaceIsNotFiltered() {
    val log = formatAppCrashLog(listOf(" ------ not a separator", "Tabby: marker"))

    assertEquals("------ not a separator\nTabby: marker", log)
  }

  @Test
  fun dumpResultUsesLogForSuccessfulLogcatExit() {
    assertEquals(
      AppCrashLogDumpResult.UseLog("AndroidRuntime: crash"),
      appCrashLogDumpResult(exitCode = 0, log = "AndroidRuntime: crash"),
    )
  }

  @Test
  fun dumpResultReportsLogcatExitCodeFailures() {
    assertEquals(
      AppCrashLogDumpResult.LogcatFailed("logcat exited with code 2: AndroidRuntime: crash"),
      appCrashLogDumpResult(exitCode = 2, log = "AndroidRuntime: crash"),
    )
  }

  @Test
  fun loadActionUsesLogForSuccessfulLogcatExit() {
    assertEquals(
      AppCrashLogLoadAction.UseLog("AndroidRuntime: crash"),
      appCrashLogLoadAction(exitCode = 0, log = "AndroidRuntime: crash"),
    )
  }

  @Test
  fun loadActionReportsFailedLogcatExit() {
    assertEquals(
      AppCrashLogLoadAction.Fail("logcat exited with code 2: AndroidRuntime: crash"),
      appCrashLogLoadAction(exitCode = 2, log = "AndroidRuntime: crash"),
    )
  }

  @Test
  fun loadActionMapsExistingDumpResult() {
    assertEquals(
      AppCrashLogLoadAction.UseLog("Tabby: marker"),
      appCrashLogLoadAction(AppCrashLogDumpResult.UseLog("Tabby: marker")),
    )
    assertEquals(
      AppCrashLogLoadAction.Fail("logcat exited with code 1:"),
      appCrashLogLoadAction(AppCrashLogDumpResult.LogcatFailed("logcat exited with code 1:")),
    )
  }

  @Test
  fun loadFailureMessageIncludesCause() {
    assertEquals(
      "Failed to load crash logs: java.lang.IllegalStateException: logcat failed",
      appCrashLogLoadFailureMessage(IllegalStateException("logcat failed")),
    )
  }
}
