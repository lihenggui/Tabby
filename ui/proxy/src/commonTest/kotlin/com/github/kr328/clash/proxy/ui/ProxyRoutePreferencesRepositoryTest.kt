package com.github.kr328.clash.proxy.ui

import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.settingsstore.TabbyProxyRoutePreferencesRepository
import com.github.kr328.clash.settingsstore.asStoreProvider
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class ProxyRoutePreferencesRepositoryTest {
  @Test
  fun inMemoryRepositoryReturnsInitialPreferences() {
    val repository =
      InMemoryProxyRoutePreferencesRepository(
        ProxyRoutePreferences(
          proxyLine = 2,
          excludeNotSelectable = true,
          proxySort = ProxySort.Title,
          lastGroupName = "Auto",
        )
      )

    assertEquals(
      ProxyRoutePreferences(
        proxyLine = 2,
        excludeNotSelectable = true,
        proxySort = ProxySort.Title,
        lastGroupName = "Auto",
      ),
      repository.query(),
    )
  }

  @Test
  fun inMemoryRepositoryUpdatesPreferencesIndependently() {
    val repository = InMemoryProxyRoutePreferencesRepository()

    repository.setLastGroupName("Fallback")
    repository.setExcludeNotSelectable(true)
    repository.setProxyLine(1)
    repository.setProxySort(ProxySort.Delay)

    assertEquals(
      ProxyRoutePreferences(
        proxyLine = 1,
        excludeNotSelectable = true,
        proxySort = ProxySort.Delay,
        lastGroupName = "Fallback",
      ),
      repository.query(),
    )
  }

  @Test
  fun settingsStoreRepositoryUsesStoredUiProxyLineDefault() {
    val repository =
      SettingsStoreProxyRoutePreferencesRepository(
        TabbyProxyRoutePreferencesRepository(MapSettings().asStoreProvider())
      )

    assertEquals(ProxyRoutePreferences(proxyLine = 2), repository.query())
  }

  @Test
  fun settingsStoreRepositoryMapsPersistedValues() {
    val settings = MapSettings()
    val firstRepository =
      SettingsStoreProxyRoutePreferencesRepository(
        TabbyProxyRoutePreferencesRepository(settings.asStoreProvider())
      )

    firstRepository.setLastGroupName("Fallback")
    firstRepository.setExcludeNotSelectable(true)
    firstRepository.setProxyLine(1)
    firstRepository.setProxySort(ProxySort.Delay)

    val secondRepository =
      SettingsStoreProxyRoutePreferencesRepository(
        TabbyProxyRoutePreferencesRepository(settings.asStoreProvider())
      )

    assertEquals(
      ProxyRoutePreferences(
        proxyLine = 1,
        excludeNotSelectable = true,
        proxySort = ProxySort.Delay,
        lastGroupName = "Fallback",
      ),
      secondRepository.query(),
    )
  }
}
