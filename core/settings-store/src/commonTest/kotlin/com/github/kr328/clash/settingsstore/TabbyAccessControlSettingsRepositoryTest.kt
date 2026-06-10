package com.github.kr328.clash.settingsstore

import com.github.kr328.clash.core.model.AccessControlSort
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyAccessControlSettingsRepositoryTest {
  @Test
  fun queryUsesDefaultsWhenKeysAreAbsent() {
    val repository = testRepository()
    val defaults =
      TabbyAccessControlSettings(
        selectedPackages = setOf("one", "two"),
        sort = AccessControlSort.InstallTime,
        reverse = true,
        showSystemApps = true,
      )

    assertEquals(defaults, repository.query(defaults))
  }

  @Test
  fun settersPersistValuesOverDefaults() {
    val repository = testRepository()

    repository.setSelectedPackages(setOf("com.example.alpha", "com.example.beta"))
    repository.setSort(AccessControlSort.UpdateTime)
    repository.setReverse(true)
    repository.setShowSystemApps(true)

    assertEquals(
      TabbyAccessControlSettings(
        selectedPackages = setOf("com.example.alpha", "com.example.beta"),
        sort = AccessControlSort.UpdateTime,
        reverse = true,
        showSystemApps = true,
      ),
      repository.query(
        defaults =
          TabbyAccessControlSettings(
            selectedPackages = setOf("com.example.other"),
            sort = AccessControlSort.PackageName,
            reverse = false,
            showSystemApps = false,
          )
      ),
    )
  }

  @Test
  fun settersPersistAcrossRepositoryInstances() {
    val uiSettings = MapSettings()
    val serviceSettings = MapSettings()
    val firstRepository =
      TabbyAccessControlSettingsRepository(
        uiStoreProvider = uiSettings.asStoreProvider(),
        serviceStoreProvider = serviceSettings.asStoreProvider(),
      )

    firstRepository.setSelectedPackages(setOf("com.example.alpha", "com.example.beta"))
    firstRepository.setSort(AccessControlSort.PackageName)
    firstRepository.setReverse(true)
    firstRepository.setShowSystemApps(true)

    val secondRepository =
      TabbyAccessControlSettingsRepository(
        uiStoreProvider = uiSettings.asStoreProvider(),
        serviceStoreProvider = serviceSettings.asStoreProvider(),
      )

    assertEquals(
      TabbyAccessControlSettings(
        selectedPackages = setOf("com.example.alpha", "com.example.beta"),
        sort = AccessControlSort.PackageName,
        reverse = true,
        showSystemApps = true,
      ),
      secondRepository.query(),
    )
  }

  private fun testRepository(): TabbyAccessControlSettingsRepository {
    return TabbyAccessControlSettingsRepository(
      uiStoreProvider = MapSettings().asStoreProvider(),
      serviceStoreProvider = MapSettings().asStoreProvider(),
    )
  }
}
