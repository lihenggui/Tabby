package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Provider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProviderItemStateMapperTest {
  @Test
  fun usesProviderTypeAndNameAsStableKey() {
    assertEquals(
      "Proxy-Provider",
      providerItemStateKey(provider(name = "Provider", type = Provider.Type.Proxy)),
    )
    assertEquals(
      "Rule-Provider",
      providerItemStateKey(provider(name = "Provider", type = Provider.Type.Rule)),
    )
  }

  @Test
  fun updatesMatchingProviderStateOnly() {
    val proxy = provider(name = "Shared", type = Provider.Type.Proxy)
    val rule = provider(name = "Shared", type = Provider.Type.Rule)
    val states =
      listOf(
        ProviderItemState(provider = proxy, updatedAt = 10, updating = false),
        ProviderItemState(provider = rule, updatedAt = 20, updating = false),
      )

    val updated =
      states.updateProviderItemState(proxy) { state -> state.copy(updating = true, updatedAt = 30) }

    assertTrue(updated[0].updating)
    assertEquals(30, updated[0].updatedAt)
    assertFalse(updated[1].updating)
    assertEquals(20, updated[1].updatedAt)
  }

  @Test
  fun mergesFetchedProvidersWithExistingState() {
    val existingProvider = provider(name = "Remote", updatedAt = 100)
    val fetchedProvider =
      existingProvider.copy(vehicleType = Provider.VehicleType.File, updatedAt = 150)
    val newProvider = provider(name = "New", updatedAt = 80)

    val states =
      mergeProviderItemStates(
        existingStates =
          listOf(ProviderItemState(provider = existingProvider, updatedAt = 120, updating = false)),
        providers = listOf(fetchedProvider, newProvider),
      )

    assertEquals(fetchedProvider, states[0].provider)
    assertEquals(150, states[0].updatedAt)
    assertFalse(states[0].updating)
    assertEquals(newProvider, states[1].provider)
    assertEquals(80, states[1].updatedAt)
    assertFalse(states[1].updating)
  }

  @Test
  fun keepsPreviousUpdatedAtWhileProviderIsUpdating() {
    val existingProvider = provider(name = "Remote", updatedAt = 100)
    val fetchedProvider = existingProvider.copy(updatedAt = 200)

    val states =
      mergeProviderItemStates(
        existingStates =
          listOf(ProviderItemState(provider = existingProvider, updatedAt = 125, updating = true)),
        providers = listOf(fetchedProvider),
      )

    assertEquals(fetchedProvider, states.single().provider)
    assertEquals(125, states.single().updatedAt)
    assertTrue(states.single().updating)
  }

  @Test
  fun keepsExistingNewerUpdatedAtWhenProviderPayloadIsOlder() {
    val existingProvider = provider(name = "Remote", updatedAt = 300)
    val fetchedProvider = existingProvider.copy(updatedAt = 100)

    val states =
      mergeProviderItemStates(
        existingStates =
          listOf(ProviderItemState(provider = existingProvider, updatedAt = 250, updating = false)),
        providers = listOf(fetchedProvider),
      )

    assertEquals(250, states.single().updatedAt)
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
