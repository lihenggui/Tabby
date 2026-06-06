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
  fun broadcastEventBoundaryMapsPayloadsToActions() {
    assertEquals(
      ProfilesBroadcastAction.FetchProfiles,
      profilesBroadcastAction(ProfilesBroadcastEvent(ProfilesBroadcastEventKind.ProfileLoaded)),
    )
    assertEquals(
      ProfilesBroadcastAction.ShowUpdateCompleted(uuid),
      profilesBroadcastAction(
        ProfilesBroadcastEvent(ProfilesBroadcastEventKind.ProfileUpdateCompleted, uuid = uuid)
      ),
    )
    assertEquals(
      ProfilesBroadcastAction.ShowUpdateFailed(uuid, reason = "network"),
      profilesBroadcastAction(
        ProfilesBroadcastEvent(
          kind = ProfilesBroadcastEventKind.ProfileUpdateFailed,
          uuid = uuid,
          reason = "network",
        )
      ),
    )
  }

  @Test
  fun platformPayloadBoundaryDropsFieldsForNonUpdateEvents() {
    assertEquals(
      ProfilesBroadcastEvent(ProfilesBroadcastEventKind.ProfileLoaded),
      profilesBroadcastEventFromPlatformPayload(
        kind = ProfilesBroadcastEventKind.ProfileLoaded,
        uuid = uuid,
        reason = "ignored",
      ),
    )
    assertEquals(
      ProfilesBroadcastEvent(ProfilesBroadcastEventKind.ProfileUpdateCompleted, uuid = uuid),
      profilesBroadcastEventFromPlatformPayload(
        kind = ProfilesBroadcastEventKind.ProfileUpdateCompleted,
        uuid = uuid,
        reason = "ignored",
      ),
    )
    assertEquals(
      ProfilesBroadcastEvent(
        kind = ProfilesBroadcastEventKind.ProfileUpdateFailed,
        uuid = uuid,
        reason = "network",
      ),
      profilesBroadcastEventFromPlatformPayload(
        kind = ProfilesBroadcastEventKind.ProfileUpdateFailed,
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
    assertEquals(
      ProfilesBroadcastAction.Ignore,
      profilesBroadcastAction(
        ProfilesBroadcastEvent(
          kind = ProfilesBroadcastEventKind.ProfileUpdateFailed,
          uuid = null,
          reason = "network",
        )
      ),
    )
  }
}
