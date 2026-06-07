package com.github.kr328.clash.app

import com.github.kr328.clash.crash.model.CrashLogRepository

internal fun emptyTabbyCrashLogRepository(): CrashLogRepository = EmptyCrashLogRepository

private object EmptyCrashLogRepository : CrashLogRepository {
  override suspend fun loadCrashLog(): String = ""
}
