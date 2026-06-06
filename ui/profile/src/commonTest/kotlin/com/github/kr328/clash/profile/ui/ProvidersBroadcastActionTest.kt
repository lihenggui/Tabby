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
      }
  }
}
