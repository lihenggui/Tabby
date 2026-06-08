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
    listOf(
        ProvidersPlatformBroadcastEventKind.ServiceRecreated to
          ProvidersBroadcastEventKind.ServiceRecreated,
        ProvidersPlatformBroadcastEventKind.Started to ProvidersBroadcastEventKind.Started,
        ProvidersPlatformBroadcastEventKind.Stopped to ProvidersBroadcastEventKind.Stopped,
        ProvidersPlatformBroadcastEventKind.ProfileChanged to
          ProvidersBroadcastEventKind.ProfileChanged,
        ProvidersPlatformBroadcastEventKind.ProfileUpdateCompleted to
          ProvidersBroadcastEventKind.ProfileUpdateCompleted,
        ProvidersPlatformBroadcastEventKind.ProfileUpdateFailed to
          ProvidersBroadcastEventKind.ProfileUpdateFailed,
        ProvidersPlatformBroadcastEventKind.ProfileLoaded to
          ProvidersBroadcastEventKind.ProfileLoaded,
      )
      .forEach { (platformKind, routeKind) ->
        assertEquals(
          ProvidersBroadcastEvent(routeKind),
          providersBroadcastEventFromPlatformPayload(platformKind),
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
