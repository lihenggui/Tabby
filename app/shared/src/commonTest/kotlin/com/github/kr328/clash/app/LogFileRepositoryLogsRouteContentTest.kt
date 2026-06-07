package com.github.kr328.clash.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class LogFileRepositoryLogsRouteContentTest {
  @Test
  fun emptyLogFileRepositoryHasNoFilesAndDeletesWithoutFailure() = runTest {
    val repository = emptyTabbyLogFileRepository()

    assertEquals(emptyList(), repository.queryLogFiles())
    repository.deleteAllLogFiles()
    assertEquals(emptyList(), repository.queryLogFiles())
  }
}
