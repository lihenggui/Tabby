package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Provider

fun providerRouteItemsAfterFetch(
  existingItems: List<ProviderRouteItem>,
  fetchedProviders: List<Provider>,
): List<ProviderRouteItem> {
  return mergeProviderItemStates(
      existingStates = existingItems.toProviderItemStates(),
      providers = sortProvidersForDisplay(fetchedProviders),
    )
    .toProviderRouteItems()
}

fun providerRouteItemsAfterUpdateStarted(
  items: List<ProviderRouteItem>,
  provider: Provider,
): List<ProviderRouteItem> {
  return items
    .toProviderItemStates()
    .updateProviderItemState(provider) { state -> state.copy(updating = true) }
    .toProviderRouteItems()
}

fun providerRouteItemsAfterUpdateFinished(
  items: List<ProviderRouteItem>,
  provider: Provider,
): List<ProviderRouteItem> {
  return items
    .toProviderItemStates()
    .updateProviderItemState(provider) { state -> state.copy(updating = false) }
    .toProviderRouteItems()
}

fun providerRouteItemsPendingUpdateProviders(items: List<ProviderRouteItem>): List<Provider> {
  return ProvidersUiState(providers = items.toProviderItemStates()).providersPendingUpdate()
}

private fun List<ProviderRouteItem>.toProviderItemStates(): List<ProviderItemState> {
  return map { item ->
    ProviderItemState(
      provider = item.provider,
      updatedAt = item.updatedAt,
      updating = item.updating,
    )
  }
}

private fun List<ProviderItemState>.toProviderRouteItems(): List<ProviderRouteItem> {
  return map { state ->
    ProviderRouteItem(
      provider = state.provider,
      updating = state.updating,
      updatedAt = state.updatedAt,
    )
  }
}
