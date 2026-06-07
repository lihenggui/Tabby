package com.github.kr328.clash.settings.ui

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.common.di.AppInfoProvider.Companion.instance as appInfoProvider
import com.github.kr328.clash.common.util.componentName
import com.github.kr328.clash.common.util.mainActivityAlias
import com.github.kr328.clash.core.model.DarkMode
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.store.UiStore
import com.github.kr328.clash.glue.util.ApplicationObserver
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper

@Composable
internal fun AppSettingsScreen(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val appContext = context.applicationContext
  val uiStore = remember(appContext) { UiStore(appContext) }
  val serviceStore = remember(appContext) { ServiceStore(appContext) }
  val clashRunning by Remote.broadcasts.clashRunningFlow.collectAsStateWithLifecycle()

  AppSettingsRouteContent(
    darkMode = uiStore.darkMode,
    modifier = modifier,
    clashRunning = clashRunning,
    initialAutoRestart = appContext.autoRestartValue,
    initialHideAppIcon = uiStore.hideAppIcon,
    initialHideFromRecents = uiStore.hideFromRecents,
    initialDynamicNotification = serviceStore.dynamicNotification,
    onAutoRestartChange = { value -> appContext.autoRestartValue = value },
    onDarkModeChange = { value -> uiStore.darkMode = value },
    onHideAppIconChange = { value ->
      appContext.hideAppIcon(value)
      uiStore.hideAppIcon = value
    },
    onHideFromRecentsChange = { value ->
      ApplicationObserver.createdActivities.forEach { it.recreate() }
      uiStore.hideFromRecents = value
    },
    onDynamicNotificationChange = { value -> serviceStore.dynamicNotification = value },
  )
}

private var Context.autoRestartValue: Boolean
  get() {
    val status =
      packageManager.getComponentEnabledSetting(appInfoProvider.restartReceiverClass.componentName)
    return appSettingsAutoRestartEnabledFromPlatformComponentState(
      state = status,
      enabledState = PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
      disabledState = PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
    )
  }
  set(value) {
    val status =
      appSettingsAutoRestartPlatformComponentState(
        autoRestart = value,
        enabledState = PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        disabledState = PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        defaultState = PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
      )

    packageManager.setComponentEnabledSetting(
      appInfoProvider.restartReceiverClass.componentName,
      status,
      PackageManager.DONT_KILL_APP,
    )
  }

private fun Context.hideAppIcon(hide: Boolean) {
  val newState =
    appSettingsHideAppIconPlatformComponentState(
      hideAppIcon = hide,
      enabledState = PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
      disabledState = PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
      defaultState = PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
    )
  packageManager.setComponentEnabledSetting(
    mainActivityAlias,
    newState,
    PackageManager.DONT_KILL_APP,
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
