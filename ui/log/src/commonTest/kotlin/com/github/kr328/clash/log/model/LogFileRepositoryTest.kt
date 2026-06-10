package com.github.kr328.clash.log.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class LogFileRepositoryTest {
  @Test
  fun queryLogFilesSelectsLogFilesFromStorageFileNames() = runTest {
    val storage =
      FakeLogFileStorage(
        fileNames =
          listOf(
            "clash-10.log",
            "invalid.log",
            "clash-30.log",
            "clash-20.log",
          )
      )
    val repository = LogFileRepository(storage)

    assertEquals(
      listOf(
        LogFile(fileName = "clash-30.log", created = 30),
        LogFile(fileName = "clash-20.log", created = 20),
        LogFile(fileName = "clash-10.log", created = 10),
      ),
      repository.queryLogFiles(),
    )
  }

  @Test
  fun deleteAllLogFilesDelegatesToStorage() = runTest {
    val storage = FakeLogFileStorage()
    val repository = LogFileRepository(storage)

    repository.deleteAllLogFiles()

    assertEquals(1, storage.deleteAllCalls)
  }

  private class FakeLogFileStorage(private val fileNames: List<String> = emptyList()) :
    LogFileStorage {
    var deleteAllCalls = 0

    override suspend fun listLogFileNames(): List<String> = fileNames

    override suspend fun deleteAllLogFiles() {
      deleteAllCalls += 1
    }
  }
}
