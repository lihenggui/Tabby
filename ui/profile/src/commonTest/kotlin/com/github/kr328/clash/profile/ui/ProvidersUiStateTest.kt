package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Provider
import kotlin.test.Test
import kotlin.test.assertEquals

class ProvidersUiStateTest {
  @Test
  fun fetchedProvidersMergeWithExistingItemStateAndPreserveCurrentTime() {
    val existingProvider = provider(name = "Remote", updatedAt = 100)
    val fetchedProvider = existingProvider.copy(updatedAt = 200)

    val state =
      ProvidersUiState(
          providers =
            listOf(
              ProviderItemState(provider = existingProvider, updatedAt = 150, updating = true)
            ),
          currentTime = 123,
        )
        .withFetchedProviders(listOf(fetchedProvider))

    assertEquals(fetchedProvider, state.providers.single().provider)
    assertEquals(150, state.providers.single().updatedAt)
    assertEquals(true, state.providers.single().updating)
    assertEquals(123, state.currentTime)
  }

  @Test
  fun fetchedProvidersAreSortedInCommonDisplayOrder() {
    val ruleProvider = provider(name = "Rule", type = Provider.Type.Rule, updatedAt = 100)
    val proxyB = provider(name = "B", type = Provider.Type.Proxy, updatedAt = 100)
    val proxyA = provider(name = "A", type = Provider.Type.Proxy, updatedAt = 200)

    val state =
      ProvidersUiState(
          providers = listOf(ProviderItemState(provider = proxyA, updatedAt = 300, updating = true))
        )
        .withFetchedProviders(listOf(ruleProvider, proxyB, proxyA))

    assertEquals(listOf(proxyA, proxyB, ruleProvider), state.providers.map { it.provider })
    assertEquals(true, state.providers[0].updating)
    assertEquals(300, state.providers[0].updatedAt)
  }

  @Test
  fun providerStateUpdateChangesMatchingProviderOnly() {
    val proxy = provider(name = "Shared", type = Provider.Type.Proxy)
    val rule = provider(name = "Shared", type = Provider.Type.Rule)
    val state =
      ProvidersUiState(
          providers =
            listOf(
              ProviderItemState(provider = proxy, updatedAt = 10, updating = false),
              ProviderItemState(provider = rule, updatedAt = 20, updating = false),
            ),
          currentTime = 123,
        )
        .withProviderState(proxy) { it.copy(updating = true, updatedAt = 30) }

    assertEquals(true, state.providers[0].updating)
    assertEquals(30, state.providers[0].updatedAt)
    assertEquals(false, state.providers[1].updating)
    assertEquals(20, state.providers[1].updatedAt)
    assertEquals(123, state.currentTime)
  }

  @Test
  fun providerUpdateStartedMarksMatchingProviderUpdatingOnly() {
    val proxy = provider(name = "Shared", type = Provider.Type.Proxy)
    val rule = provider(name = "Shared", type = Provider.Type.Rule)
    val state =
      ProvidersUiState(
          providers =
            listOf(
              ProviderItemState(provider = proxy, updatedAt = 10, updating = false),
              ProviderItemState(provider = rule, updatedAt = 20, updating = false),
            ),
          currentTime = 123,
        )
        .withProviderUpdateStarted(proxy)

    assertEquals(true, state.providers[0].updating)
    assertEquals(10, state.providers[0].updatedAt)
    assertEquals(false, state.providers[1].updating)
    assertEquals(20, state.providers[1].updatedAt)
    assertEquals(123, state.currentTime)
  }

  @Test
  fun providerUpdateSucceededMarksMatchingProviderFinishedAndReplacesUpdatedAt() {
    val proxy = provider(name = "Shared", type = Provider.Type.Proxy)
    val rule = provider(name = "Shared", type = Provider.Type.Rule)
    val state =
      ProvidersUiState(
          providers =
            listOf(
              ProviderItemState(provider = proxy, updatedAt = 10, updating = true),
              ProviderItemState(provider = rule, updatedAt = 20, updating = true),
            ),
          currentTime = 123,
        )
        .withProviderUpdateSucceeded(proxy, updatedAt = 30)

    assertEquals(false, state.providers[0].updating)
    assertEquals(30, state.providers[0].updatedAt)
    assertEquals(true, state.providers[1].updating)
    assertEquals(20, state.providers[1].updatedAt)
    assertEquals(123, state.currentTime)
  }

  @Test
  fun providerUpdateFailedMarksMatchingProviderFinishedOnly() {
    val proxy = provider(name = "Shared", type = Provider.Type.Proxy)
    val rule = provider(name = "Shared", type = Provider.Type.Rule)
    val state =
      ProvidersUiState(
          providers =
            listOf(
              ProviderItemState(provider = proxy, updatedAt = 10, updating = true),
              ProviderItemState(provider = rule, updatedAt = 20, updating = true),
            ),
          currentTime = 123,
        )
        .withProviderUpdateFailed(proxy)

    assertEquals(false, state.providers[0].updating)
    assertEquals(10, state.providers[0].updatedAt)
    assertEquals(true, state.providers[1].updating)
    assertEquals(20, state.providers[1].updatedAt)
    assertEquals(123, state.currentTime)
  }

  @Test
  fun currentTimeUpdatePreservesProviders() {
    val states = listOf(ProviderItemState(provider("Remote"), updatedAt = 100, updating = false))

    val state = ProvidersUiState(providers = states, currentTime = 123).withCurrentTime(456)

    assertEquals(states, state.providers)
    assertEquals(456, state.currentTime)
  }

  @Test
  fun pendingUpdateProvidersExcludeInlineAndAlreadyUpdatingProviders() {
    val remote = provider(name = "Remote")
    val inline = provider(name = "Inline", vehicleType = Provider.VehicleType.Inline)
    val updating = provider(name = "Updating")
    val state =
      ProvidersUiState(
        providers =
          listOf(
            ProviderItemState(provider = remote, updatedAt = 0, updating = false),
            ProviderItemState(provider = inline, updatedAt = 0, updating = false),
            ProviderItemState(provider = updating, updatedAt = 0, updating = true),
          )
      )

    assertEquals(listOf(remote), state.providersPendingUpdate())
  }

  @Test
  fun updateAllActionUpdatesPendingProvidersOnly() {
    val remote = provider(name = "Remote")
    val inline = provider(name = "Inline", vehicleType = Provider.VehicleType.Inline)
    val updating = provider(name = "Updating")
    val state =
      ProvidersUiState(
        providers =
          listOf(
            ProviderItemState(provider = remote, updatedAt = 0, updating = false),
            ProviderItemState(provider = inline, updatedAt = 0, updating = false),
            ProviderItemState(provider = updating, updatedAt = 0, updating = true),
          )
      )

    assertEquals(
      ProvidersUpdateAllAction.UpdateProviders(listOf(remote)),
      providersUpdateAllAction(state),
    )
  }

  @Test
  fun updateAllActionIgnoresWhenNoProvidersArePending() {
    val inline = provider(name = "Inline", vehicleType = Provider.VehicleType.Inline)
    val updating = provider(name = "Updating")
    val state =
      ProvidersUiState(
        providers =
          listOf(
            ProviderItemState(provider = inline, updatedAt = 0, updating = false),
            ProviderItemState(provider = updating, updatedAt = 0, updating = true),
          )
      )

    assertEquals(ProvidersUpdateAllAction.Ignore, providersUpdateAllAction(state))
  }

  private fun provider(
    name: String,
    type: Provider.Type = Provider.Type.Proxy,
    vehicleType: Provider.VehicleType = Provider.VehicleType.HTTP,
    updatedAt: Long = 0,
  ): Provider {
    return Provider(
      name = name,
      type = type,
      vehicleType = vehicleType,
      updatedAt = updatedAt,
    )
  }
}
