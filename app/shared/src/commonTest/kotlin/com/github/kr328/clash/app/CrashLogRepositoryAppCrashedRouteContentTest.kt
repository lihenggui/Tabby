package com.github.kr328.clash.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class CrashLogRepositoryAppCrashedRouteContentTest {
  @Test
  fun emptyCrashLogRepositoryReturnsEmptyLog() = runTest {
    assertEquals("", emptyTabbyCrashLogRepository().loadCrashLog())
  }

  @Test
  fun loadFailureMessageIncludesCause() {
    assertEquals(
      "Failed to load crash logs: java.lang.IllegalStateException: logcat failed",
      tabbyCrashLogLoadFailureMessage(IllegalStateException("logcat failed")),
    )
  }
}
