package com.github.kr328.clash.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.core.model.DarkMode
import com.github.kr328.clash.settings.vm.AppSettingsViewModel
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper

@Composable
internal fun AppSettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: AppSettingsViewModel = viewModel(),
) {
  val clashRunning by viewModel.clashRunning.collectAsStateWithLifecycle()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  AppSettingsContent(
    clashRunning = clashRunning,
    uiState = uiState,
    onAutoRestartChange = viewModel::updateAutoRestart,
    onDarkModeChange = viewModel::updateDarkMode,
    onHideAppIconChange = viewModel::updateHideAppIcon,
    onHideFromRecentsChange = viewModel::updateHideFromRecents,
    onDynamicNotificationChange = viewModel::updateDynamicNotification,
    modifier = modifier,
  )
}

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun AppSettingsScreenPreview() {
  AppSettingsContent(
    clashRunning = false,
    uiState =
      AppSettingsUiState(
        autoRestart = true,
        darkMode = DarkMode.Auto,
        hideAppIcon = false,
        hideFromRecents = false,
        dynamicNotification = true,
      ),
    onAutoRestartChange = {},
    onDarkModeChange = {},
    onHideAppIconChange = {},
    onHideFromRecentsChange = {},
    onDynamicNotificationChange = {},
  )
}

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun AppSettingsScreenRunningPreview() {
  AppSettingsContent(
    clashRunning = true,
    uiState =
      AppSettingsUiState(
        autoRestart = true,
        darkMode = DarkMode.ForceDark,
        hideAppIcon = true,
        hideFromRecents = true,
        dynamicNotification = true,
      ),
    onAutoRestartChange = {},
    onDarkModeChange = {},
    onHideAppIconChange = {},
    onHideFromRecentsChange = {},
    onDynamicNotificationChange = {},
  )
}
