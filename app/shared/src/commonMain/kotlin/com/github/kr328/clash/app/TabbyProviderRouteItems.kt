package com.github.kr328.clash.app

import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.profile.ui.ProviderRouteItem

internal fun tabbyProviderRouteItemsAfterFetch(
  existingItems: List<ProviderRouteItem>,
  fetchedProviders: List<Provider>,
): List<ProviderRouteItem> {
  val existingItemsByKey = existingItems.associateBy { item -> item.provider.tabbyProviderKey() }

  return fetchedProviders.sorted().map { provider ->
    val existing = existingItemsByKey[provider.tabbyProviderKey()]

    if (existing == null) {
      ProviderRouteItem(provider = provider)
    } else {
      existing.copy(
        provider = provider,
        updatedAt =
          if (existing.updating) existing.updatedAt
          else maxOf(existing.updatedAt, provider.updatedAt),
      )
    }
  }
}

internal fun tabbyProviderRouteItemsAfterUpdateStarted(
  items: List<ProviderRouteItem>,
  provider: Provider,
): List<ProviderRouteItem> {
  return items.map { item ->
    if (item.provider.hasSameTabbyProviderKey(provider)) item.copy(updating = true) else item
  }
}

internal fun tabbyProviderRouteItemsAfterUpdateFinished(
  items: List<ProviderRouteItem>,
  provider: Provider,
): List<ProviderRouteItem> {
  return items.map { item ->
    if (item.provider.hasSameTabbyProviderKey(provider)) item.copy(updating = false) else item
  }
}

internal fun tabbyProviderRouteItemsPendingUpdateProviders(
  items: List<ProviderRouteItem>
): List<Provider> {
  return items
    .filterNot { item ->
      item.updating || item.provider.vehicleType == Provider.VehicleType.Inline
    }
    .map { item -> item.provider }
}

private fun Provider.hasSameTabbyProviderKey(other: Provider): Boolean {
  return tabbyProviderKey() == other.tabbyProviderKey()
}

private fun Provider.tabbyProviderKey(): String {
  return "$type-$name"
}
