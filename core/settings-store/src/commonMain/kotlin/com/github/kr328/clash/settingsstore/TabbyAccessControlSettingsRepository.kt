package com.github.kr328.clash.settingsstore

import com.github.kr328.clash.common.store.Store
import com.github.kr328.clash.common.store.StoreProvider
import com.github.kr328.clash.core.model.AccessControlSort

data class TabbyAccessControlSettings(
  val selectedPackages: Set<String> = emptySet(),
  val sort: AccessControlSort = AccessControlSort.Label,
  val reverse: Boolean = false,
  val showSystemApps: Boolean = false,
)

class TabbyAccessControlSettingsRepository(
  private val uiStoreProvider: StoreProvider,
  private val serviceStoreProvider: StoreProvider,
) {
  private val uiStore = Store(uiStoreProvider)
  private val serviceStore = Store(serviceStoreProvider)

  private var storedSelectedPackages by
    serviceStore.stringSet(ACCESS_CONTROL_PACKAGES_KEY, emptySet())
  private var storedSort by
    uiStore.enum(
      ACCESS_CONTROL_SORT_KEY,
      AccessControlSort.Label,
      AccessControlSort.entries.toTypedArray(),
    )
  private var storedReverse by uiStore.boolean(ACCESS_CONTROL_REVERSE_KEY, false)
  private var storedShowSystemApps by uiStore.boolean(ACCESS_CONTROL_SYSTEM_APP_KEY, false)

  fun query(
    defaults: TabbyAccessControlSettings = TabbyAccessControlSettings()
  ): TabbyAccessControlSettings {
    return TabbyAccessControlSettings(
      selectedPackages =
        if (serviceStoreProvider.contains(ACCESS_CONTROL_PACKAGES_KEY)) storedSelectedPackages
        else defaults.selectedPackages,
      sort = if (uiStoreProvider.contains(ACCESS_CONTROL_SORT_KEY)) storedSort else defaults.sort,
      reverse =
        if (uiStoreProvider.contains(ACCESS_CONTROL_REVERSE_KEY)) storedReverse
        else defaults.reverse,
      showSystemApps =
        if (uiStoreProvider.contains(ACCESS_CONTROL_SYSTEM_APP_KEY)) storedShowSystemApps
        else defaults.showSystemApps,
    )
  }

  fun setSelectedPackages(value: Set<String>) {
    storedSelectedPackages = value
  }

  fun setSort(value: AccessControlSort) {
    storedSort = value
  }

  fun setReverse(value: Boolean) {
    storedReverse = value
  }

  fun setShowSystemApps(value: Boolean) {
    storedShowSystemApps = value
  }

  private companion object {
    const val ACCESS_CONTROL_PACKAGES_KEY = "access_control_packages"
    const val ACCESS_CONTROL_SORT_KEY = "access_control_sort"
    const val ACCESS_CONTROL_REVERSE_KEY = "access_control_reverse"
    const val ACCESS_CONTROL_SYSTEM_APP_KEY = "access_control_system_app"
  }
}
