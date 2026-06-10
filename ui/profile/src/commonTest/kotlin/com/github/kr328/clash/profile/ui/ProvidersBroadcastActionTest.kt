package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ProvidersBroadcastActionTest {
  @Test
  fun profileLoadedFetchesProviders() {
    assertEquals(
      ProvidersBroadcastAction.FetchProviders,
      providersBroadcastAction(ProvidersBroadcastEventKind.ProfileLoaded),
    )
    assertEquals(
      ProvidersBroadcastAction.FetchProviders,
      providersBroadcastAction(ProvidersBroadcastEvent(ProvidersBroadcastEventKind.ProfileLoaded)),
    )
  }

  @Test
  fun nonProfileLoadedEventsAreIgnored() {
    listOf(
        ProvidersBroadcastEventKind.ServiceRecreated,
        ProvidersBroadcastEventKind.Started,
        ProvidersBroadcastEventKind.Stopped,
        ProvidersBroadcastEventKind.ProfileChanged,
        ProvidersBroadcastEventKind.ProfileUpdateCompleted,
        ProvidersBroadcastEventKind.ProfileUpdateFailed,
      )
      .forEach { kind ->
        assertEquals(ProvidersBroadcastAction.Ignore, providersBroadcastAction(kind))
        assertEquals(
          ProvidersBroadcastAction.Ignore,
          providersBroadcastAction(ProvidersBroadcastEvent(kind)),
        )
      }
  }

  @Test
  fun broadcastEventFromSourceMapsAllKinds() {
    val expected =
      mapOf(
        ProvidersBroadcastSourceEventKind.ServiceRecreated to
          ProvidersBroadcastEventKind.ServiceRecreated,
        ProvidersBroadcastSourceEventKind.Started to ProvidersBroadcastEventKind.Started,
        ProvidersBroadcastSourceEventKind.Stopped to ProvidersBroadcastEventKind.Stopped,
        ProvidersBroadcastSourceEventKind.ProfileChanged to
          ProvidersBroadcastEventKind.ProfileChanged,
        ProvidersBroadcastSourceEventKind.ProfileUpdateCompleted to
          ProvidersBroadcastEventKind.ProfileUpdateCompleted,
        ProvidersBroadcastSourceEventKind.ProfileUpdateFailed to
          ProvidersBroadcastEventKind.ProfileUpdateFailed,
        ProvidersBroadcastSourceEventKind.ProfileLoaded to
          ProvidersBroadcastEventKind.ProfileLoaded,
      )

    expected.forEach { (sourceKind, eventKind) ->
      assertEquals(
        ProvidersBroadcastEvent(eventKind),
        providersBroadcastEventFromSource(sourceKind),
      )
    }
  }

  @Test
  fun broadcastEventFromPlatformPayloadMapsGenericSourcePayload() {
    assertEquals(
      ProvidersBroadcastEvent(ProvidersBroadcastEventKind.ProfileLoaded),
      providersBroadcastEventFromPlatformPayload(
        event = ProvidersBroadcastSourcePayload(ProvidersBroadcastSourceEventKind.ProfileLoaded),
        kind = ProvidersBroadcastSourcePayload::kind,
      ),
    )
    assertEquals(
      ProvidersBroadcastEvent(ProvidersBroadcastEventKind.ProfileUpdateFailed),
      providersBroadcastEventFromPlatformPayload(
        event =
          ProvidersBroadcastSourcePayload(ProvidersBroadcastSourceEventKind.ProfileUpdateFailed),
        kind = ProvidersBroadcastSourcePayload::kind,
      ),
    )
  }

  private data class ProvidersBroadcastSourcePayload(val kind: ProvidersBroadcastSourceEventKind)
}
