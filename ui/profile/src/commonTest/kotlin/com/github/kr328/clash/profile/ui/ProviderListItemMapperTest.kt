package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Provider
import kotlin.test.Test
import kotlin.test.assertEquals

class ProviderListItemMapperTest {
  @Test
  fun mapsProviderDisplayState() {
    val provider = Provider("Proxy Provider", Provider.Type.Proxy, Provider.VehicleType.HTTP, 100)

    val item = provider.toProviderListItem(currentTime = 1000, updatedAt = 700, updating = true)

    assertEquals(provider, item.provider)
    assertEquals("Proxy/HTTP", item.typeText)
    assertEquals("elapsed:300", item.updatedAtText)
    assertEquals(true, item.updating)
  }

  @Test
  fun usesStateUpdatedAtInsteadOfProviderUpdatedAt() {
    val provider = Provider("Rule Provider", Provider.Type.Rule, Provider.VehicleType.File, 100)

    val item = provider.toProviderListItem(currentTime = 1000, updatedAt = 900, updating = false)

    assertEquals("elapsed:100", item.updatedAtText)
    assertEquals(false, item.updating)
  }

  @Test
  fun mapsProviderUiStateProvidersInOrderUsingStateFields() {
    val first = Provider("Proxy Provider", Provider.Type.Proxy, Provider.VehicleType.HTTP, 100)
    val second = Provider("Rule Provider", Provider.Type.Rule, Provider.VehicleType.File, 500)

    val items =
      ProvidersUiState(
          providers =
            listOf(
              ProviderItemState(provider = first, updatedAt = 900, updating = true),
              ProviderItemState(provider = second, updatedAt = 700, updating = false),
            ),
          currentTime = 1000,
        )
        .toProviderListItems(
          formatType = { "${it.type}/${it.vehicleType}" },
          formatElapsedMillis = { "elapsed:$it" },
        )

    assertEquals(listOf(first, second), items.map(ProviderListItem::provider))
    assertEquals(listOf("Proxy/HTTP", "Rule/File"), items.map(ProviderListItem::typeText))
    assertEquals(listOf("elapsed:100", "elapsed:300"), items.map(ProviderListItem::updatedAtText))
    assertEquals(listOf(true, false), items.map(ProviderListItem::updating))
  }

  private fun Provider.toProviderListItem(
    currentTime: Long,
    updatedAt: Long,
    updating: Boolean,
  ): ProviderListItem {
    return toProviderListItem(
      currentTime = currentTime,
      updatedAt = updatedAt,
      updating = updating,
      formatType = { "${it.type}/${it.vehicleType}" },
      formatElapsedMillis = { "elapsed:$it" },
    )
  }
}
