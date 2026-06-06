package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Provider

internal data class ProvidersUiState(
  val providers: List<ProviderItemState> = emptyList(),
  val currentTime: Long = 0,
)

internal sealed interface ProvidersUpdateAllAction {
  data class UpdateProviders(val providers: List<Provider>) : ProvidersUpdateAllAction

  data object Ignore : ProvidersUpdateAllAction
}

internal sealed interface ProvidersEventState {
  data object Idle : ProvidersEventState

  data class ShowMessage(val message: String) : ProvidersEventState
}

internal fun sortProvidersForDisplay(providers: List<Provider>): List<Provider> {
  return providers.sorted()
}

internal fun ProvidersUiState.withFetchedProviders(providers: List<Provider>): ProvidersUiState {
  return copy(
    providers = mergeProviderItemStates(this.providers, sortProvidersForDisplay(providers))
  )
}

internal fun ProvidersUiState.withProviderState(
  provider: Provider,
  transform: (ProviderItemState) -> ProviderItemState,
): ProvidersUiState {
  return copy(providers = providers.updateProviderItemState(provider, transform))
}

internal fun ProvidersUiState.withProviderUpdateStarted(provider: Provider): ProvidersUiState {
  return withProviderState(provider) { state -> state.copy(updating = true) }
}

internal fun ProvidersUiState.withProviderUpdateSucceeded(
  provider: Provider,
  updatedAt: Long,
): ProvidersUiState {
  return withProviderState(provider) { state ->
    state.copy(updating = false, updatedAt = updatedAt)
  }
}

internal fun ProvidersUiState.withProviderUpdateFailed(provider: Provider): ProvidersUiState {
  return withProviderState(provider) { state -> state.copy(updating = false) }
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

internal fun providersUpdateAllAction(state: ProvidersUiState): ProvidersUpdateAllAction {
  val providers = state.providersPendingUpdate()

  return if (providers.isEmpty()) ProvidersUpdateAllAction.Ignore
  else ProvidersUpdateAllAction.UpdateProviders(providers)
}

internal fun providerUpdateFailureEventState(
  providerName: String,
  errorMessage: String,
  formatUpdateFailure: (String, String) -> String,
): ProvidersEventState {
  return ProvidersEventState.ShowMessage(formatUpdateFailure(providerName, errorMessage))
}
