package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Provider

internal data class ProvidersUiState(
  val providers: List<ProviderItemState> = emptyList(),
  val currentTime: Long = 0,
)

internal fun ProvidersUiState.withFetchedProviders(providers: List<Provider>): ProvidersUiState {
  return copy(providers = mergeProviderItemStates(this.providers, providers))
}

internal fun ProvidersUiState.withProviderState(
  provider: Provider,
  transform: (ProviderItemState) -> ProviderItemState,
): ProvidersUiState {
  return copy(providers = providers.updateProviderItemState(provider, transform))
}

internal fun ProvidersUiState.withCurrentTime(currentTime: Long): ProvidersUiState {
  return copy(currentTime = currentTime)
}

internal fun ProvidersUiState.providersPendingUpdate(): List<Provider> {
  return providers
    .filterNot { state ->
      state.updating || state.provider.vehicleType == Provider.VehicleType.Inline
    }
    .map { state -> state.provider }
}
