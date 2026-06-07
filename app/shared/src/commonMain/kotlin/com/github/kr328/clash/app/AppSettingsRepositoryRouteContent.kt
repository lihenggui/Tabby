package com.github.kr328.clash.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.DarkMode
import com.github.kr328.clash.settings.ui.AppSettingsRouteContent
import com.github.kr328.clash.settingsstore.TabbyAppSettings
import com.github.kr328.clash.settingsstore.TabbyAppSettingsRepository

@Composable
internal fun AppSettingsRepositoryRouteContent(
  repository: TabbyAppSettingsRepository,
  darkMode: DarkMode,
  onDarkModeChange: (DarkMode) -> Unit,
  modifier: Modifier = Modifier,
  clashRunning: Boolean = false,
  defaults: TabbyAppSettings = TabbyAppSettings(darkMode = darkMode),
  onAutoRestartChange: (Boolean) -> Unit = {},
  onHideAppIconChange: (Boolean) -> Unit = {},
  onHideFromRecentsChange: (Boolean) -> Unit = {},
  onDynamicNotificationChange: (Boolean) -> Unit = {},
) {
  val settings = remember(repository, defaults) { repository.query(defaults) }

  AppSettingsRouteContent(
    darkMode = settings.darkMode,
    modifier = modifier,
    clashRunning = clashRunning,
    initialAutoRestart = settings.autoRestart,
    initialHideAppIcon = settings.hideAppIcon,
    initialHideFromRecents = settings.hideFromRecents,
    initialDynamicNotification = settings.dynamicNotification,
    onAutoRestartChange = { value ->
      repository.setAutoRestart(value)
      onAutoRestartChange(value)
    },
    onDarkModeChange = { value ->
      repository.setDarkMode(value)
      onDarkModeChange(value)
    },
    onHideAppIconChange = { value ->
      repository.setHideAppIcon(value)
      onHideAppIconChange(value)
    },
    onHideFromRecentsChange = { value ->
      repository.setHideFromRecents(value)
      onHideFromRecentsChange(value)
    },
    onDynamicNotificationChange = { value ->
      repository.setDynamicNotification(value)
      onDynamicNotificationChange(value)
    },
  )
}
