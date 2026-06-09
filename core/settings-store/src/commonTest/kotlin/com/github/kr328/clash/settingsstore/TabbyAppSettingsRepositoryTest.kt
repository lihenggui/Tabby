package com.github.kr328.clash.settingsstore

import com.github.kr328.clash.core.model.DarkMode
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TabbyAppSettingsRepositoryTest {
  @Test
  fun queryUsesDefaultsWhenKeysAreAbsent() {
    val repository = testRepository()
    val defaults =
      TabbyAppSettings(
        autoRestart = true,
        darkMode = DarkMode.ForceDark,
        hideAppIcon = true,
        hideFromRecents = true,
        dynamicNotification = false,
      )

    assertEquals(defaults, repository.query(defaults))
  }

  @Test
  fun settersPersistValuesOverDefaults() {
    val repository = testRepository()

    repository.setAutoRestart(true)
    repository.setDarkMode(DarkMode.ForceDark)
    repository.setHideAppIcon(true)
    repository.setHideFromRecents(true)
    repository.setDynamicNotification(false)

    assertEquals(
      TabbyAppSettings(
        autoRestart = true,
        darkMode = DarkMode.ForceDark,
        hideAppIcon = true,
        hideFromRecents = true,
        dynamicNotification = false,
      ),
      repository.query(defaults = TabbyAppSettings(darkMode = DarkMode.ForceLight)),
    )
  }

  @Test
  fun settersPersistAcrossRepositoryInstances() {
    val uiSettings = MapSettings()
    val serviceSettings = MapSettings()
    val firstRepository =
      TabbyAppSettingsRepository(
        uiStoreProvider = uiSettings.asStoreProvider(),
        serviceStoreProvider = serviceSettings.asStoreProvider(),
      )

    firstRepository.setAutoRestart(true)
    firstRepository.setDarkMode(DarkMode.ForceDark)
    firstRepository.setHideAppIcon(true)
    firstRepository.setHideFromRecents(true)
    firstRepository.setDynamicNotification(false)

    val secondRepository =
      TabbyAppSettingsRepository(
        uiStoreProvider = uiSettings.asStoreProvider(),
        serviceStoreProvider = serviceSettings.asStoreProvider(),
      )

    assertEquals(
      TabbyAppSettings(
        autoRestart = true,
        darkMode = DarkMode.ForceDark,
        hideAppIcon = true,
        hideFromRecents = true,
        dynamicNotification = false,
      ),
      secondRepository.query(defaults = TabbyAppSettings(darkMode = DarkMode.ForceLight)),
    )
  }

  @Test
  fun hasDarkModeTracksStoredDarkModePreference() {
    val repository = testRepository()

    assertFalse(repository.hasDarkMode)

    repository.setDarkMode(DarkMode.ForceLight)

    assertTrue(repository.hasDarkMode)
  }

  private fun testRepository(): TabbyAppSettingsRepository {
    return TabbyAppSettingsRepository(
      uiStoreProvider = MapSettings().asStoreProvider(),
      serviceStoreProvider = MapSettings().asStoreProvider(),
    )
  }
}
