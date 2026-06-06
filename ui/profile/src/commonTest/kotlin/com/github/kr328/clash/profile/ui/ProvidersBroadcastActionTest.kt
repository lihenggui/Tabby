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
  fun platformPayloadBoundaryCreatesProviderBroadcastEvent() {
    ProvidersBroadcastEventKind.entries.forEach { kind ->
      assertEquals(
        ProvidersBroadcastEvent(kind),
        providersBroadcastEventFromPlatformPayload(kind),
      )
    }
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
}
