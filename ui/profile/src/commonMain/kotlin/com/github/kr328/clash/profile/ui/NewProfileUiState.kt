package com.github.kr328.clash.profile.ui

internal data class NewProfileUiState<T>(val providers: List<T> = emptyList())

internal fun <T> NewProfileUiState<T>.withNewProfileProviders(
  providers: List<T>
): NewProfileUiState<T> {
  return copy(providers = providers)
}
