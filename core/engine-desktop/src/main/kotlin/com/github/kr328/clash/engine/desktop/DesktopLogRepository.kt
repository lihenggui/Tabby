package com.github.kr328.clash.engine.desktop

import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.engine.api.LogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class DesktopLogRepository(private val mihomoApi: DesktopMihomoApi? = null) : LogRepository {
  override fun observeLogs(): Flow<LogMessage> {
    return mihomoApi?.observeLogs() ?: emptyFlow()
  }
}
