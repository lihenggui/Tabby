package com.github.kr328.clash.log.model

interface LogFileStorage {
  suspend fun listLogFileNames(): List<String>

  suspend fun deleteAllLogFiles()
}
