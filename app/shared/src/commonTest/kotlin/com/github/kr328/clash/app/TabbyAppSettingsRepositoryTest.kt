package com.github.kr328.clash.app

import com.github.kr328.clash.core.model.DarkMode
import com.github.kr328.clash.settingsstore.asStoreProvider
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
