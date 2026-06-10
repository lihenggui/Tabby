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
  fun broadcastEventFromSourceMapsAllKinds() {
    val expected =
      mapOf(
        ProfilesBroadcastSourceEventKind.ServiceRecreated to
          ProfilesBroadcastEventKind.ServiceRecreated,
        ProfilesBroadcastSourceEventKind.Started to ProfilesBroadcastEventKind.Started,
        ProfilesBroadcastSourceEventKind.Stopped to ProfilesBroadcastEventKind.Stopped,
        ProfilesBroadcastSourceEventKind.ProfileChanged to
          ProfilesBroadcastEventKind.ProfileChanged,
        ProfilesBroadcastSourceEventKind.ProfileUpdateCompleted to
          ProfilesBroadcastEventKind.ProfileUpdateCompleted,
        ProfilesBroadcastSourceEventKind.ProfileUpdateFailed to
          ProfilesBroadcastEventKind.ProfileUpdateFailed,
        ProfilesBroadcastSourceEventKind.ProfileLoaded to ProfilesBroadcastEventKind.ProfileLoaded,
      )

    expected.forEach { (sourceKind, eventKind) ->
      assertEquals(
        ProfilesBroadcastEvent(eventKind),
        profilesBroadcastEventFromSource(sourceKind),
      )
    }
  }

  @Test
  fun broadcastEventFromSourceKeepsUpdatePayloadsOnlyForUpdateEvents() {
    assertEquals(
      ProfilesBroadcastEvent(
        kind = ProfilesBroadcastEventKind.ProfileUpdateCompleted,
        uuid = uuid,
      ),
      profilesBroadcastEventFromSource(
        kind = ProfilesBroadcastSourceEventKind.ProfileUpdateCompleted,
        uuid = uuid,
        reason = "unused",
      ),
    )
    assertEquals(
      ProfilesBroadcastEvent(
        kind = ProfilesBroadcastEventKind.ProfileUpdateFailed,
        uuid = uuid,
        reason = "network",
      ),
      profilesBroadcastEventFromSource(
        kind = ProfilesBroadcastSourceEventKind.ProfileUpdateFailed,
        uuid = uuid,
        reason = "network",
      ),
    )
    assertEquals(
      ProfilesBroadcastEvent(ProfilesBroadcastEventKind.ProfileLoaded),
      profilesBroadcastEventFromSource(
        kind = ProfilesBroadcastSourceEventKind.ProfileLoaded,
        uuid = uuid,
        reason = "unused",
      ),
    )
  }

  @Test
  fun broadcastEventFromPlatformPayloadMapsGenericSourcePayload() {
    assertEquals(
      ProfilesBroadcastEvent(
        kind = ProfilesBroadcastEventKind.ProfileUpdateCompleted,
        uuid = uuid,
      ),
      profilesBroadcastEventFromPlatformPayload(
        event =
          ProfilesBroadcastSourcePayload(
            kind = ProfilesBroadcastSourceEventKind.ProfileUpdateCompleted,
            uuid = uuid,
            reason = "unused",
          ),
        kind = ProfilesBroadcastSourcePayload::kind,
        uuid = ProfilesBroadcastSourcePayload::uuid,
        reason = ProfilesBroadcastSourcePayload::reason,
      ),
    )
    assertEquals(
      ProfilesBroadcastEvent(
        kind = ProfilesBroadcastEventKind.ProfileUpdateFailed,
        uuid = uuid,
        reason = "network",
      ),
      profilesBroadcastEventFromPlatformPayload(
        event =
          ProfilesBroadcastSourcePayload(
            kind = ProfilesBroadcastSourceEventKind.ProfileUpdateFailed,
            uuid = uuid,
            reason = "network",
          ),
        kind = ProfilesBroadcastSourcePayload::kind,
        uuid = ProfilesBroadcastSourcePayload::uuid,
        reason = ProfilesBroadcastSourcePayload::reason,
      ),
    )
    assertEquals(
      ProfilesBroadcastEvent(ProfilesBroadcastEventKind.ProfileLoaded),
      profilesBroadcastEventFromPlatformPayload(
        event =
          ProfilesBroadcastSourcePayload(
            kind = ProfilesBroadcastSourceEventKind.ProfileLoaded,
            uuid = uuid,
            reason = "unused",
          ),
        kind = ProfilesBroadcastSourcePayload::kind,
        uuid = ProfilesBroadcastSourcePayload::uuid,
        reason = ProfilesBroadcastSourcePayload::reason,
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

  private data class ProfilesBroadcastSourcePayload(
    val kind: ProfilesBroadcastSourceEventKind,
    val uuid: Uuid? = null,
    val reason: String? = null,
  )
}
