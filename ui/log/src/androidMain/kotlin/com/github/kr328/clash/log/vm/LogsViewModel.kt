package com.github.kr328.clash.log.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.glue.util.logsDir
import com.github.kr328.clash.log.model.LogFile
import com.github.kr328.clash.log.model.logFilesInitialState
import com.github.kr328.clash.log.model.selectLogFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class LogsViewModel(app: Application) : AndroidViewModel(app) {
  val logFiles: StateFlow<List<LogFile>>
    field = MutableStateFlow(logFilesInitialState())

  init {
    viewModelScope.launch { logFiles.value = loadAllLogs() }
  }

  fun deleteAll() {
    viewModelScope.launch {
      deleteAllLogs()
      logFiles.value = loadAllLogs()
    }
  }

  private suspend fun loadAllLogs(): List<LogFile> =
    withContext(Dispatchers.IO) {
      selectLogFiles(application.logsDir.listFiles()?.map { it.name }.orEmpty())
    }

  private suspend fun deleteAllLogs() =
    withContext(Dispatchers.IO) { application.logsDir.deleteRecursively() }
}
