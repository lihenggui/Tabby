package com.github.kr328.clash.settings.ui

import com.github.kr328.clash.core.model.DarkMode

internal enum class AppComponentEnabledState {
  Enabled,
  Disabled,
  Unspecified,
}

internal fun appComponentEnabledStateFromPlatformState(
  platformState: Int,
  enabledState: Int,
  disabledState: Int,
): AppComponentEnabledState {
  return when (platformState) {
    enabledState -> AppComponentEnabledState.Enabled
    disabledState -> AppComponentEnabledState.Disabled
    else -> AppComponentEnabledState.Unspecified
  }
}

internal fun appComponentEnabledPlatformState(
  componentState: AppComponentEnabledState,
  enabledState: Int,
  disabledState: Int,
  defaultState: Int,
): Int {
  return when (componentState) {
    AppComponentEnabledState.Enabled -> enabledState
    AppComponentEnabledState.Disabled -> disabledState
    AppComponentEnabledState.Unspecified -> defaultState
  }
}

internal fun isAppSettingsAutoRestartEnabled(componentState: AppComponentEnabledState): Boolean {
  return componentState == AppComponentEnabledState.Enabled
}

internal fun appSettingsAutoRestartComponentState(autoRestart: Boolean): AppComponentEnabledState {
  return if (autoRestart) {
    AppComponentEnabledState.Enabled
  } else {
    AppComponentEnabledState.Disabled
  }
}

internal fun appSettingsHideAppIconComponentState(hideAppIcon: Boolean): AppComponentEnabledState {
  return if (hideAppIcon) {
    AppComponentEnabledState.Disabled
  } else {
    AppComponentEnabledState.Enabled
  }
}

internal fun updateAppSettingsAutoRestart(
  uiState: AppSettingsUiState,
  autoRestart: Boolean,
): AppSettingsUiState {
  return uiState.copy(autoRestart = autoRestart)
}

internal fun updateAppSettingsDarkMode(
  uiState: AppSettingsUiState,
  darkMode: DarkMode,
): AppSettingsUiState {
  return uiState.copy(darkMode = darkMode)
}

internal fun updateAppSettingsHideAppIcon(
  uiState: AppSettingsUiState,
  hideAppIcon: Boolean,
): AppSettingsUiState {
  return uiState.copy(hideAppIcon = hideAppIcon)
}

internal fun updateAppSettingsHideFromRecents(
  uiState: AppSettingsUiState,
  hideFromRecents: Boolean,
): AppSettingsUiState {
  return uiState.copy(hideFromRecents = hideFromRecents)
}

internal fun updateAppSettingsDynamicNotification(
  uiState: AppSettingsUiState,
  dynamicNotification: Boolean,
): AppSettingsUiState {
  return uiState.copy(dynamicNotification = dynamicNotification)
}
