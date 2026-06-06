package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class ProfilesBroadcastActionTest {
  private val uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")

  @Test
  fun serviceAndProfileReloadEventsFetchProfiles() {
    listOf(
        ProfilesBroadcastEventKind.ServiceRecreated,
        ProfilesBroadcastEventKind.Started,
        ProfilesBroadcastEventKind.ProfileChanged,
        ProfilesBroadcastEventKind.ProfileLoaded,
      )
      .forEach { kind ->
        assertEquals(ProfilesBroadcastAction.FetchProfiles, profilesBroadcastAction(kind))
      }
  }

  @Test
  fun stoppedEventsAreIgnored() {
    assertEquals(
      ProfilesBroadcastAction.Ignore,
      profilesBroadcastAction(ProfilesBroadcastEventKind.Stopped),
    )
  }

  @Test
  fun profileUpdateEventsShowMessagesWhenUuidExists() {
    assertEquals(
      ProfilesBroadcastAction.ShowUpdateCompleted(uuid),
      profilesBroadcastAction(ProfilesBroadcastEventKind.ProfileUpdateCompleted, uuid = uuid),
    )
    assertEquals(
      ProfilesBroadcastAction.ShowUpdateFailed(uuid, reason = "network"),
      profilesBroadcastAction(
        ProfilesBroadcastEventKind.ProfileUpdateFailed,
        uuid = uuid,
        reason = "network",
      ),
    )
  }

  @Test
  fun profileUpdateEventsAreIgnoredWhenUuidIsMissing() {
    assertEquals(
      ProfilesBroadcastAction.Ignore,
      profilesBroadcastAction(ProfilesBroadcastEventKind.ProfileUpdateCompleted),
    )
    assertEquals(
      ProfilesBroadcastAction.Ignore,
      profilesBroadcastAction(ProfilesBroadcastEventKind.ProfileUpdateFailed, reason = "network"),
    )
  }
}
