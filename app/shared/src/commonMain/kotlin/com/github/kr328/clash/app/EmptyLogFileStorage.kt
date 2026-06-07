package com.github.kr328.clash.app

import com.github.kr328.clash.log.model.LogFileRepository
import com.github.kr328.clash.log.model.LogFileStorage

internal fun emptyTabbyLogFileRepository(): LogFileRepository =
  LogFileRepository(EmptyLogFileStorage)

private object EmptyLogFileStorage : LogFileStorage {
  override suspend fun listLogFileNames(): List<String> = emptyList()

  override suspend fun deleteAllLogFiles() {}
}
