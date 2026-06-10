package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Provider

internal data class ProviderItemState(
  val provider: Provider,
  val updatedAt: Long,
  val updating: Boolean,
)

internal fun providerItemStateKey(provider: Provider): String {
  return "${provider.type}-${provider.name}"
}

internal fun List<ProviderItemState>.updateProviderItemState(
  provider: Provider,
  transform: (ProviderItemState) -> ProviderItemState,
): List<ProviderItemState> {
  val key = providerItemStateKey(provider)

  return map { state ->
    if (providerItemStateKey(state.provider) == key) transform(state) else state
  }
}

internal fun mergeProviderItemStates(
  existingStates: List<ProviderItemState>,
  providers: List<Provider>,
): List<ProviderItemState> {
  val existingMap = existingStates.associateBy { providerItemStateKey(it.provider) }

  return providers.map { provider ->
    val existing = existingMap[providerItemStateKey(provider)]
    if (existing != null) {
      existing.copy(
        provider = provider,
        updatedAt =
          if (existing.updating) existing.updatedAt
          else maxOf(existing.updatedAt, provider.updatedAt),
      )
    } else {
      ProviderItemState(provider = provider, updatedAt = provider.updatedAt, updating = false)
    }
  }
}
