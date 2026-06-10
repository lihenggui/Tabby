package com.github.kr328.clash.engine.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProfileSnapshotRefreshRuleTest {
  @Test
  fun profileRepositoryBroadcastEventsRefreshOnlyProfileSnapshotsThatCanChange() {
    val cases =
      listOf(
        ProfileRepositoryBroadcastEventKind.ServiceRecreated to false,
        ProfileRepositoryBroadcastEventKind.Started to false,
        ProfileRepositoryBroadcastEventKind.Stopped to false,
        ProfileRepositoryBroadcastEventKind.ProfileChanged to true,
        ProfileRepositoryBroadcastEventKind.ProfileUpdateCompleted to true,
        ProfileRepositoryBroadcastEventKind.ProfileUpdateFailed to true,
        ProfileRepositoryBroadcastEventKind.ProfileLoaded to true,
      )

    cases.forEach { (kind, expected) ->
      assertEquals(expected, profileRepositoryRequiresProfileSnapshotRefresh(kind), kind.name)
    }
  }

  @Test
  fun profileRepositoryBroadcastEventFromPlatformPayloadUsesSourceKind() {
    assertTrue(
      profileRepositoryRequiresProfileSnapshotRefreshFromPlatformPayload(
        event =
          ProfileRepositoryBroadcastSourcePayload(
            ProfileRepositoryBroadcastEventKind.ProfileLoaded
          ),
        kind = ProfileRepositoryBroadcastSourcePayload::kind,
      )
    )
    assertFalse(
      profileRepositoryRequiresProfileSnapshotRefreshFromPlatformPayload(
        event =
          ProfileRepositoryBroadcastSourcePayload(ProfileRepositoryBroadcastEventKind.Stopped),
        kind = ProfileRepositoryBroadcastSourcePayload::kind,
      )
    )
  }

  private data class ProfileRepositoryBroadcastSourcePayload(
    val kind: ProfileRepositoryBroadcastEventKind
  )
}
