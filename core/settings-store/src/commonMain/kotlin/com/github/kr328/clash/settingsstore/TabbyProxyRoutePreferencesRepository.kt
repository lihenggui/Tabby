package com.github.kr328.clash.settingsstore

import com.github.kr328.clash.common.store.Store
import com.github.kr328.clash.common.store.StoreProvider
import com.github.kr328.clash.core.model.ProxySort

data class TabbyProxyRoutePreferences(
  val proxyLine: Int = 2,
  val excludeNotSelectable: Boolean = false,
  val proxySort: ProxySort = ProxySort.Default,
  val lastGroupName: String = "",
)

class TabbyProxyRoutePreferencesRepository(private val uiStoreProvider: StoreProvider) {
  private val uiStore = Store(uiStoreProvider)

  private var storedProxyLine by uiStore.int(PROXY_LINE_KEY, 2)
  private var storedExcludeNotSelectable by uiStore.boolean(PROXY_EXCLUDE_NOT_SELECTABLE_KEY, false)
  private var storedProxySort by
    uiStore.enum(PROXY_SORT_KEY, ProxySort.Default, ProxySort.entries.toTypedArray())
  private var storedLastGroupName by uiStore.string(PROXY_LAST_GROUP_KEY, "")

  fun query(
    defaults: TabbyProxyRoutePreferences = TabbyProxyRoutePreferences()
  ): TabbyProxyRoutePreferences {
    return TabbyProxyRoutePreferences(
      proxyLine =
        if (uiStoreProvider.contains(PROXY_LINE_KEY)) storedProxyLine else defaults.proxyLine,
      excludeNotSelectable =
        if (uiStoreProvider.contains(PROXY_EXCLUDE_NOT_SELECTABLE_KEY)) storedExcludeNotSelectable
        else defaults.excludeNotSelectable,
      proxySort =
        if (uiStoreProvider.contains(PROXY_SORT_KEY)) storedProxySort else defaults.proxySort,
      lastGroupName =
        if (uiStoreProvider.contains(PROXY_LAST_GROUP_KEY)) storedLastGroupName
        else defaults.lastGroupName,
    )
  }

  fun setProxyLine(value: Int) {
    storedProxyLine = value
  }

  fun setExcludeNotSelectable(value: Boolean) {
    storedExcludeNotSelectable = value
  }

  fun setProxySort(value: ProxySort) {
    storedProxySort = value
  }

  fun setLastGroupName(value: String) {
    storedLastGroupName = value
  }

  private companion object {
    const val PROXY_LINE_KEY = "proxy_line"
    const val PROXY_EXCLUDE_NOT_SELECTABLE_KEY = "proxy_exclude_not_selectable"
    const val PROXY_SORT_KEY = "proxy_sort"
    const val PROXY_LAST_GROUP_KEY = "proxy_last_group"
  }
}
