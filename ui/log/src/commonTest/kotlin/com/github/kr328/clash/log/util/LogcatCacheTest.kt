package com.github.kr328.clash.log.util

import com.github.kr328.clash.core.model.LogMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class LogcatCacheTest {
  @Test
  fun incrementalSnapshotReturnsNullWhenCacheHasNotChanged() = runTest {
    val cache = LogcatCache()

    assertNull(cache.snapshot(full = false))
  }

  @Test
  fun incrementalSnapshotReturnsAppendedMessagesAndResetsCounters() = runTest {
    val cache = LogcatCache()
    val message = logMessage(1)

    cache.append(message)

    assertEquals(
      LogcatCache.Snapshot(listOf(message), removed = 0, appended = 1),
      cache.snapshot(full = false),
    )
    assertNull(cache.snapshot(full = false))
  }

  @Test
  fun cacheDropsOldestMessagesWhenCapacityIsExceeded() = runTest {
    val cache = LogcatCache()
    val messages = List(LogcatCache.CAPACITY + 1) { index -> logMessage(index.toLong()) }

    messages.forEach { cache.append(it) }

    assertEquals(
      LogcatCache.Snapshot(
        messages = messages.drop(1),
        removed = 1,
        appended = LogcatCache.CAPACITY,
      ),
      cache.snapshot(full = false),
    )
  }

  @Test
  fun fullSnapshotReturnsCurrentMessagesAndResetsCounters() = runTest {
    val cache = LogcatCache()
    val messages = listOf(logMessage(1), logMessage(2))

    messages.forEach { cache.append(it) }

    assertEquals(
      LogcatCache.Snapshot(messages = messages, removed = 0, appended = 4),
      cache.snapshot(full = true),
    )
    assertNull(cache.snapshot(full = false))
  }

  private fun logMessage(time: Long): LogMessage {
    return LogMessage(LogMessage.Level.Info, "message-$time", time)
  }
}
