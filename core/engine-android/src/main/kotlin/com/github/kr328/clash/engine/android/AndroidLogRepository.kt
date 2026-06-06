package com.github.kr328.clash.engine.android

import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.engine.api.LogRepository
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.service.remote.IClashManager
import com.github.kr328.clash.service.remote.ILogObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class AndroidLogRepository : LogRepository {
  override fun observeLogs(): Flow<LogMessage> = observeAndroidLogs {
    Remote.service.remote.get().clash()
  }
}

fun observeAndroidLogs(clash: IClashManager): Flow<LogMessage> = observeAndroidLogs {
  clash
}

private fun observeAndroidLogs(clashProvider: suspend () -> IClashManager): Flow<LogMessage> =
  callbackFlow {
      val clash = clashProvider()
      val observer =
        object : ILogObserver {
          override fun newItem(log: String) {
            trySend(decodeAndroidLogMessage(log))
          }
        }

      clash.setLogObserver(observer)

      awaitClose { runCatching { clash.setLogObserver(null) } }
    }
    .flowOn(Dispatchers.IO)

private val json = Json {
  ignoreUnknownKeys = true
}

internal fun decodeAndroidLogMessage(log: String): LogMessage = json.decodeFromString(log)
