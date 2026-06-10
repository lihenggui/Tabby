package com.github.kr328.clash.log.model

class LogFileRepository(private val storage: LogFileStorage) {
  suspend fun queryLogFiles(): List<LogFile> {
    return selectLogFiles(storage.listLogFileNames())
  }

  suspend fun deleteAllLogFiles() {
    storage.deleteAllLogFiles()
  }
}
