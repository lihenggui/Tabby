package com.github.kr328.clash.engine.api

import com.github.kr328.clash.core.model.LogMessage
import kotlinx.coroutines.flow.Flow

interface LogRepository {
  fun observeLogs(): Flow<LogMessage>
}
