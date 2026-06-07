package com.github.kr328.clash.log.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class EmptyLogFileRepositoryTest {
  @Test
  fun emptyLogFileRepositoryHasNoFilesAndDeletesWithoutFailure() = runTest {
    val repository = emptyLogFileRepository()

    assertEquals(emptyList(), repository.queryLogFiles())
    repository.deleteAllLogFiles()
    assertEquals(emptyList(), repository.queryLogFiles())
  }
}
