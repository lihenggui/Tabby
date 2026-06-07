package com.github.kr328.clash.profile.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.Provider
import org.jetbrains.compose.resources.stringResource
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.compatible
import tabby.ui.shared.generated.resources.file
import tabby.ui.shared.generated.resources.format_provider_type
import tabby.ui.shared.generated.resources.http
import tabby.ui.shared.generated.resources.inline
import tabby.ui.shared.generated.resources.proxy
import tabby.ui.shared.generated.resources.rule

data class ProviderRouteItem(
  val provider: Provider,
  val updating: Boolean = false,
  val updatedAt: Long = provider.updatedAt,
)

@Composable
fun ProvidersRouteContent(
  modifier: Modifier = Modifier,
  providers: List<ProviderRouteItem> = emptyList(),
  currentTimeMillis: Long = 0,
  onUpdateAll: () -> Unit = {},
  onUpdate: (Provider) -> Unit = {},
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val effectiveCurrentTime =
    maxOf(currentTimeMillis, providers.maxOfOrNull { it.updatedAt } ?: currentTimeMillis)

  ProvidersContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    providers = providers.toProviderListItems(effectiveCurrentTime),
    onUpdateAll = onUpdateAll,
    onUpdate = { _, provider -> onUpdate(provider) },
  )
}

@Composable
private fun List<ProviderRouteItem>.toProviderListItems(
  currentTimeMillis: Long
): List<ProviderListItem> {
  val items = ArrayList<ProviderListItem>(size)

  for (item in this) {
    val provider = item.provider
    items +=
      ProviderListItem(
        provider = provider,
        typeText =
          providerTypeTextString(
            providerTypeText(type = provider.type, vehicleType = provider.vehicleType)
          ),
        updatedAtText = elapsedTimeTextString(currentTimeMillis - item.updatedAt),
        updating = item.updating,
      )
  }

  return items
}

@Composable
private fun providerTypeTextString(typeText: ProviderTypeText): String {
  return stringResource(
    SharedRes.string.format_provider_type,
    typeText.typeToken.stringResource(),
    typeText.vehicleToken.stringResource(),
  )
}

@Composable
private fun ProviderTypeTextToken.stringResource(): String {
  return when (this) {
    ProviderTypeTextToken.Proxy -> stringResource(SharedRes.string.proxy)
    ProviderTypeTextToken.Rule -> stringResource(SharedRes.string.rule)
  }
}

@Composable
private fun ProviderVehicleTextToken.stringResource(): String {
  return when (this) {
    ProviderVehicleTextToken.Http -> stringResource(SharedRes.string.http)
    ProviderVehicleTextToken.File -> stringResource(SharedRes.string.file)
    ProviderVehicleTextToken.Inline -> stringResource(SharedRes.string.inline)
    ProviderVehicleTextToken.Compatible -> stringResource(SharedRes.string.compatible)
  }
}
