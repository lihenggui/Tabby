package com.github.kr328.clash.log.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.log.model.AndroidLogFileStorage
import com.github.kr328.clash.log.model.LogFile
import com.github.kr328.clash.log.model.LogFileRepository
import com.github.kr328.clash.log.model.logFilesInitialState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class LogsViewModel(app: Application) : AndroidViewModel(app) {
  val logFiles: StateFlow<List<LogFile>>
    field = MutableStateFlow(logFilesInitialState())
  private val logFileRepository = LogFileRepository(AndroidLogFileStorage(app))

  init {
    viewModelScope.launch { logFiles.value = logFileRepository.queryLogFiles() }
  }

  fun deleteAll() {
    viewModelScope.launch {
      logFileRepository.deleteAllLogFiles()
      logFiles.value = logFileRepository.queryLogFiles()
    }
  }
}
