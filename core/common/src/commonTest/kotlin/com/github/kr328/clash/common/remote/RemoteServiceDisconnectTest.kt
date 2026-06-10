package com.github.kr328.clash.common.remote

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RemoteServiceDisconnectTest {
  @Test
  fun reportsCrashWhenPreviousDisconnectWasRecent() {
    assertTrue(
      tabbyRemoteServiceShouldReportCrashOnDisconnect(
        lastDisconnectedAtMillis = 10_000L,
        currentTimeMillis = 19_999L,
      )
    )
  }

  @Test
  fun ignoresDisconnectWhenPreviousDisconnectIsAtOrBeyondCrashInterval() {
    assertFalse(
      tabbyRemoteServiceShouldReportCrashOnDisconnect(
        lastDisconnectedAtMillis = 10_000L,
        currentTimeMillis = 20_000L,
      )
    )
    assertFalse(
      tabbyRemoteServiceShouldReportCrashOnDisconnect(
        lastDisconnectedAtMillis = -1L,
        currentTimeMillis = 20_000L,
      )
    )
  }
}
