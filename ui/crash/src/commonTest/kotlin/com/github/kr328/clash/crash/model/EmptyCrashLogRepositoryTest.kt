package com.github.kr328.clash.crash.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class EmptyCrashLogRepositoryTest {
  @Test
  fun emptyCrashLogRepositoryReturnsEmptyLog() = runTest {
    assertEquals("", emptyCrashLogRepository().loadCrashLog())
  }
}
