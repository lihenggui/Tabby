package com.github.kr328.clash.crash.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.crash.model.AndroidCrashLogRepository
import com.github.kr328.clash.crash.model.CrashLogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

internal class AppCrashedViewModel(
  app: Application,
  private val crashLogRepository: CrashLogRepository = AndroidCrashLogRepository(app),
) : AndroidViewModel(app) {
  val logs: StateFlow<String> =
    flow {
        val log =
          runCatching { crashLogRepository.loadCrashLog() }
            .getOrElse { e ->
              Log.e("Failed to load crash logs", e)
              "Failed to load crash logs: ${e.stackTraceToString()}"
            }
        emit(log)
      }
      .stateIn(viewModelScope, SharingStarted.Lazily, "")
}
