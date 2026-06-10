package com.github.kr328.clash.proxy.ui

import com.github.kr328.clash.core.model.Proxy
import com.github.kr328.clash.core.model.ProxyGroup
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProxyUiStateTest {
  @Test
  fun proxyGroupBuildsItemSourcesWithLinkedGroupIndexes() {
    val group =
      ProxyGroup(
        type = Proxy.Type.Selector,
        now = "Auto",
        proxies =
          listOf(
            proxy("Auto", Proxy.Type.URLTest),
            proxy("Direct", Proxy.Type.Direct),
            proxy("Unknown group", Proxy.Type.Selector),
          ),
      )

    val sources = group.toProxyItemSources(groupNames = listOf("Proxy", "Auto"))

    assertEquals(listOf("Auto", "Direct", "Unknown group"), sources.map { it.proxy.name })
    assertEquals(listOf(1, -1, -1), sources.map { it.linkIndex })
  }

  @Test
  fun proxyGroupRefreshPreservesOnlyStillVisibleDelayTests() {
    val group =
      ProxyGroup(
        type = Proxy.Type.Selector,
        now = "A",
        proxies = listOf(proxy("A", Proxy.Type.Direct), proxy("B", Proxy.Type.Direct)),
      )
    val previous =
      ProxyGroupUiState(
        selectable = false,
        urlTesting = true,
        delayTestingKeys = setOf("A", "removed"),
        refreshVersion = 7,
      )
    val sources = group.toProxyItemSources(groupNames = listOf("Proxy"))

    val refreshed = previous.withProxyGroup(group, sources)

    assertTrue(refreshed.selectable)
    assertFalse(refreshed.urlTesting)
    assertEquals(setOf("A"), refreshed.delayTestingKeys)
    assertEquals(8, refreshed.refreshVersion)
    assertEquals(sources, refreshed.sources)
  }
}

private fun proxy(name: String, type: Proxy.Type): Proxy =
  Proxy(name = name, title = "$name title", subtitle = "$name subtitle", type = type, delay = 12)
