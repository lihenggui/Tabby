package com.github.kr328.clash.proxy.ui

import com.github.kr328.clash.core.model.ProxySort
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
}
