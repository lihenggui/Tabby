package com.github.kr328.clash.log.util

import com.github.kr328.clash.core.model.LogMessage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class LogcatCache {
  data class Snapshot(val messages: List<LogMessage>, val removed: Int, val appended: Int)

  private val messages = mutableListOf<LogMessage>()
  private val lock = Mutex()

  private var removed: Int = 0
  private var appended: Int = 0

  suspend fun append(msg: LogMessage) {
    lock.withLock {
      if (messages.size >= CAPACITY) {
        messages.removeAt(0)

        removed++
        appended--
      }

      messages.add(msg)

      appended++
    }
  }

  suspend fun snapshot(full: Boolean): Snapshot? {
    return lock.withLock {
      if (!full && removed == 0 && appended == 0) {
        return@withLock null
      }

      Snapshot(
          messages.toList(),
          removed,
          if (full) messages.size + appended else appended,
        )
        .also {
          removed = 0
          appended = 0
        }
    }
  }

  companion object {
    const val CAPACITY = 128
  }
}
