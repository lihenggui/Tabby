package com.github.kr328.clash.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class CrashLogRepositoryAppCrashedRouteContentTest {
  @Test
  fun emptyCrashLogRepositoryReturnsEmptyLog() = runTest {
    assertEquals("", emptyTabbyCrashLogRepository().loadCrashLog())
  }
}
