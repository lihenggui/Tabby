package com.github.kr328.clash.log.model

import android.content.Context
import com.github.kr328.clash.glue.util.logsDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AndroidLogFileStorage(private val context: Context) : LogFileStorage {
  override suspend fun listLogFileNames(): List<String> =
    withContext(Dispatchers.IO) { context.logsDir.listFiles()?.map { it.name }.orEmpty() }

  override suspend fun deleteAllLogFiles() {
    withContext(Dispatchers.IO) { context.logsDir.deleteRecursively() }
  }
}
