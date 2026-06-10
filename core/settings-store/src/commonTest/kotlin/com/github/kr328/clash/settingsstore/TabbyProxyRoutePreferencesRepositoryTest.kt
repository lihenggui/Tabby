package com.github.kr328.clash.settingsstore

import com.github.kr328.clash.core.model.ProxySort
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyProxyRoutePreferencesRepositoryTest {
  @Test
  fun queryUsesDefaultsWhenKeysAreAbsent() {
    val repository = testRepository()
    val defaults =
      TabbyProxyRoutePreferences(
        proxyLine = 1,
        excludeNotSelectable = true,
        proxySort = ProxySort.Title,
        lastGroupName = "Proxy",
      )

    assertEquals(defaults, repository.query(defaults))
  }

  @Test
  fun settersPersistValuesOverDefaults() {
    val repository = testRepository()

    repository.setProxyLine(3)
    repository.setExcludeNotSelectable(true)
    repository.setProxySort(ProxySort.Delay)
    repository.setLastGroupName("Fallback")

    assertEquals(
      TabbyProxyRoutePreferences(
        proxyLine = 3,
        excludeNotSelectable = true,
        proxySort = ProxySort.Delay,
        lastGroupName = "Fallback",
      ),
      repository.query(defaults = TabbyProxyRoutePreferences(proxyLine = 1)),
    )
  }

  @Test
  fun settersPersistAcrossRepositoryInstances() {
    val uiSettings = MapSettings()
    val firstRepository = TabbyProxyRoutePreferencesRepository(uiSettings.asStoreProvider())

    firstRepository.setProxyLine(3)
    firstRepository.setExcludeNotSelectable(true)
    firstRepository.setProxySort(ProxySort.Delay)
    firstRepository.setLastGroupName("Fallback")

    val secondRepository = TabbyProxyRoutePreferencesRepository(uiSettings.asStoreProvider())

    assertEquals(
      TabbyProxyRoutePreferences(
        proxyLine = 3,
        excludeNotSelectable = true,
        proxySort = ProxySort.Delay,
        lastGroupName = "Fallback",
      ),
      secondRepository.query(defaults = TabbyProxyRoutePreferences(proxyLine = 1)),
    )
  }

  private fun testRepository(): TabbyProxyRoutePreferencesRepository {
    return TabbyProxyRoutePreferencesRepository(MapSettings().asStoreProvider())
  }
}
