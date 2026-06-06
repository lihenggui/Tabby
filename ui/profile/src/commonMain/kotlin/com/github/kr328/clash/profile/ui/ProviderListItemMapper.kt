package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Provider

internal enum class ProviderTypeTextToken {
  Proxy,
  Rule,
}

internal enum class ProviderVehicleTextToken {
  Http,
  File,
  Inline,
  Compatible,
}

internal data class ProviderTypeText(
  val typeToken: ProviderTypeTextToken,
  val vehicleToken: ProviderVehicleTextToken,
)

internal fun providerTypeText(
  type: Provider.Type,
  vehicleType: Provider.VehicleType,
): ProviderTypeText {
  val typeToken =
    when (type) {
      Provider.Type.Proxy -> ProviderTypeTextToken.Proxy
      Provider.Type.Rule -> ProviderTypeTextToken.Rule
    }
  val vehicleToken =
    when (vehicleType) {
      Provider.VehicleType.HTTP -> ProviderVehicleTextToken.Http
      Provider.VehicleType.File -> ProviderVehicleTextToken.File
      Provider.VehicleType.Inline -> ProviderVehicleTextToken.Inline
      Provider.VehicleType.Compatible -> ProviderVehicleTextToken.Compatible
    }

  return ProviderTypeText(typeToken = typeToken, vehicleToken = vehicleToken)
}

internal fun Provider.toProviderListItem(
  currentTime: Long,
  updatedAt: Long,
  updating: Boolean,
  formatTypeText: (ProviderTypeText) -> String,
  formatElapsedMillis: (Long) -> String,
): ProviderListItem {
  return ProviderListItem(
    provider = this,
    typeText = formatTypeText(providerTypeText(type = type, vehicleType = vehicleType)),
    updatedAtText = formatElapsedMillis(currentTime - updatedAt),
    updating = updating,
  )
}

internal fun ProvidersUiState.toProviderListItems(
  formatTypeText: (ProviderTypeText) -> String,
  formatElapsedMillis: (Long) -> String,
): List<ProviderListItem> {
  return providers.map { state ->
    state.provider.toProviderListItem(
      currentTime = currentTime,
      updatedAt = state.updatedAt,
      updating = state.updating,
      formatTypeText = formatTypeText,
      formatElapsedMillis = formatElapsedMillis,
    )
  }
}
