package com.github.kr328.clash.log.model

fun emptyLogFileRepository(): LogFileRepository = LogFileRepository(EmptyLogFileStorage)

private object EmptyLogFileStorage : LogFileStorage {
  override suspend fun listLogFileNames(): List<String> = emptyList()

  override suspend fun deleteAllLogFiles() {}
}
