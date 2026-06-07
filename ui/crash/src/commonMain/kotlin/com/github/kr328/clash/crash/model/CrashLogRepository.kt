package com.github.kr328.clash.crash.model

interface CrashLogRepository {
  suspend fun loadCrashLog(): String
}
