package com.github.kr328.clash.app

import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.profile.ui.ProviderRouteItem
import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyProviderRouteItemsTest {
  @Test
  fun fetchedProvidersAreSortedAndMappedToRouteItems() {
    val ruleProvider = provider(name = "Rule", type = Provider.Type.Rule)
    val proxyB = provider(name = "B")
    val proxyA = provider(name = "A")

    assertEquals(
      listOf(proxyA, proxyB, ruleProvider),
      tabbyProviderRouteItemsAfterFetch(
          existingItems = emptyList(),
          fetchedProviders = listOf(ruleProvider, proxyB, proxyA),
        )
        .map { item -> item.provider },
    )
  }

  @Test
  fun fetchedProvidersPreserveUpdatingStateAndUpdatedAtForMatchingItems() {
    val existingProvider = provider(name = "HttpProvider", updatedAt = 100)
    val fetchedProvider = existingProvider.copy(updatedAt = 200)

    val items =
      tabbyProviderRouteItemsAfterFetch(
        existingItems =
          listOf(ProviderRouteItem(provider = existingProvider, updating = true, updatedAt = 150)),
        fetchedProviders = listOf(fetchedProvider),
      )

    assertEquals(
      listOf(ProviderRouteItem(provider = fetchedProvider, updating = true, updatedAt = 150)),
      items,
    )
  }

  @Test
  fun fetchedProvidersKeepLargestUpdatedAtForIdleMatchingItems() {
    val existingProvider = provider(name = "HttpProvider", updatedAt = 200)
    val olderFetchedProvider = existingProvider.copy(updatedAt = 100)

    val items =
      tabbyProviderRouteItemsAfterFetch(
        existingItems =
          listOf(ProviderRouteItem(provider = existingProvider, updating = false, updatedAt = 300)),
        fetchedProviders = listOf(olderFetchedProvider),
      )

    assertEquals(
      listOf(ProviderRouteItem(provider = olderFetchedProvider, updating = false, updatedAt = 300)),
      items,
    )
  }

  @Test
  fun updateStartedAndFinishedMatchProviderTypeAndNameOnly() {
    val proxy = provider(name = "Shared", type = Provider.Type.Proxy)
    val rule = provider(name = "Shared", type = Provider.Type.Rule)

    val started =
      tabbyProviderRouteItemsAfterUpdateStarted(
        items = listOf(ProviderRouteItem(proxy), ProviderRouteItem(rule)),
        provider = proxy.copy(updatedAt = 999),
      )

    assertEquals(true, started[0].updating)
    assertEquals(false, started[1].updating)

    val finished = tabbyProviderRouteItemsAfterUpdateFinished(started, proxy.copy(updatedAt = 999))

    assertEquals(false, finished[0].updating)
    assertEquals(false, finished[1].updating)
  }

  @Test
  fun pendingUpdateProvidersExcludeInlineAndAlreadyUpdatingItems() {
    val remote = provider(name = "HttpProvider")
    val inline = provider(name = "Inline", vehicleType = Provider.VehicleType.Inline)
    val updating = provider(name = "Updating")

    assertEquals(
      listOf(remote),
      tabbyProviderRouteItemsPendingUpdateProviders(
        listOf(
          ProviderRouteItem(provider = remote, updating = false),
          ProviderRouteItem(provider = inline, updating = false),
          ProviderRouteItem(provider = updating, updating = true),
        )
      ),
    )
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
