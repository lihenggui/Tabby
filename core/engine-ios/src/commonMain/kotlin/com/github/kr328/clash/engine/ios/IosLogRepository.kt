package com.github.kr328.clash.engine.ios

import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.engine.api.LogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class IosLogRepository : LogRepository {
  override fun observeLogs(): Flow<LogMessage> {
    return emptyFlow()
  }
}
