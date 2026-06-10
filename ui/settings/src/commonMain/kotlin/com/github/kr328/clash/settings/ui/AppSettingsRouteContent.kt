package com.github.kr328.clash.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.DarkMode

@Composable
fun AppSettingsRouteContent(
  darkMode: DarkMode,
  modifier: Modifier = Modifier,
  clashRunning: Boolean = false,
  initialAutoRestart: Boolean = false,
  initialHideAppIcon: Boolean = false,
  initialHideFromRecents: Boolean = false,
  initialDynamicNotification: Boolean = true,
  onAutoRestartChange: (Boolean) -> Unit = {},
  onDarkModeChange: (DarkMode) -> Unit = {},
  onHideAppIconChange: (Boolean) -> Unit = {},
  onHideFromRecentsChange: (Boolean) -> Unit = {},
  onDynamicNotificationChange: (Boolean) -> Unit = {},
) {
  var uiState by remember {
    mutableStateOf(
      appSettingsInitialUiState(
        autoRestart = initialAutoRestart,
        darkMode = darkMode,
        hideAppIcon = initialHideAppIcon,
        hideFromRecents = initialHideFromRecents,
        dynamicNotification = initialDynamicNotification,
      )
    )
  }

  LaunchedEffect(darkMode) {
    if (uiState.darkMode != darkMode) {
      uiState = updateAppSettingsDarkMode(uiState, darkMode)
    }
  }

  AppSettingsContent(
    clashRunning = clashRunning,
    uiState = uiState,
    onAutoRestartChange = { value ->
      uiState = updateAppSettingsAutoRestart(uiState, value)
      onAutoRestartChange(value)
    },
    onDarkModeChange = { value ->
      uiState = updateAppSettingsDarkMode(uiState, value)
      onDarkModeChange(value)
    },
    onHideAppIconChange = { value ->
      uiState = updateAppSettingsHideAppIcon(uiState, value)
      onHideAppIconChange(value)
    },
    onHideFromRecentsChange = { value ->
      uiState = updateAppSettingsHideFromRecents(uiState, value)
      onHideFromRecentsChange(value)
    },
    onDynamicNotificationChange = { value ->
      uiState = updateAppSettingsDynamicNotification(uiState, value)
      onDynamicNotificationChange(value)
    },
    modifier = modifier,
  )
}
