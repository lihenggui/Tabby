package com.github.kr328.clash.engine.android

import com.github.kr328.clash.glue.remote.Broadcasts
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AndroidProfileRepositoryTest {
  @Test
  fun profileEventsRequireSnapshotRefresh() {
    assertTrue(Broadcasts.Event.ProfileChanged.requiresProfileSnapshotRefresh())
    assertTrue(Broadcasts.Event.ProfileLoaded.requiresProfileSnapshotRefresh())
    assertTrue(
      Broadcasts.Event.ProfileUpdateCompleted(uuid = null).requiresProfileSnapshotRefresh()
    )
    assertTrue(
      Broadcasts.Event.ProfileUpdateFailed(uuid = null, reason = "network")
        .requiresProfileSnapshotRefresh()
    )
  }

  @Test
  fun serviceEventsDoNotRequireProfileSnapshotRefresh() {
    assertFalse(Broadcasts.Event.ServiceRecreated.requiresProfileSnapshotRefresh())
    assertFalse(Broadcasts.Event.Started.requiresProfileSnapshotRefresh())
    assertFalse(Broadcasts.Event.Stopped(cause = "manual").requiresProfileSnapshotRefresh())
  }
}
