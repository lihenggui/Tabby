package com.github.kr328.clash.crash.model

fun emptyCrashLogRepository(): CrashLogRepository = EmptyCrashLogRepository

private object EmptyCrashLogRepository : CrashLogRepository {
  override suspend fun loadCrashLog(): String = ""
}
