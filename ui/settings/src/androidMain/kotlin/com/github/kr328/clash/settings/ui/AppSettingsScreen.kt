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
import com.github.kr328.clash.settingsstore.TabbyAppSettings
import com.github.kr328.clash.settingsstore.TabbyAppSettingsRepository
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper

@Composable
internal fun AppSettingsScreen(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val appContext = context.applicationContext
  val uiStore = remember(appContext) { UiStore(appContext) }
  val serviceStore = remember(appContext) { ServiceStore(appContext) }
  val appSettingsRepository =
    remember(uiStore, serviceStore) {
      TabbyAppSettingsRepository(
        uiStoreProvider = uiStore.storeProvider,
        serviceStoreProvider = serviceStore.storeProvider,
      )
    }
  val clashRunning by Remote.broadcasts.clashRunningFlow.collectAsStateWithLifecycle()

  AppSettingsRepositoryRouteContent(
    repository = appSettingsRepository,
    darkMode = uiStore.darkMode,
    modifier = modifier,
    clashRunning = clashRunning,
    defaults =
      TabbyAppSettings(
        autoRestart = appContext.autoRestartValue,
        darkMode = uiStore.darkMode,
        hideAppIcon = uiStore.hideAppIcon,
        hideFromRecents = uiStore.hideFromRecents,
        dynamicNotification = serviceStore.dynamicNotification,
      ),
    onAutoRestartChange = { value -> appContext.autoRestartValue = value },
    onDarkModeChange = {},
    onHideAppIconChange = { value ->
      appContext.hideAppIcon(value)
    },
    onHideFromRecentsChange = { value ->
      ApplicationObserver.createdActivities.forEach { it.recreate() }
    },
  )
}

private var Context.autoRestartValue: Boolean
  get() {
    val status =
      packageManager
        .getComponentEnabledSetting(appInfoProvider.restartReceiverClass.componentName)
        .toAppComponentEnabledState()
    return isAppSettingsAutoRestartEnabled(status)
  }
  set(value) {
    val status = appSettingsAutoRestartComponentState(value).toPlatformComponentState()

    packageManager.setComponentEnabledSetting(
      appInfoProvider.restartReceiverClass.componentName,
      status,
      PackageManager.DONT_KILL_APP,
    )
  }

private fun Context.hideAppIcon(hide: Boolean) {
  val newState = appSettingsHideAppIconComponentState(hide).toPlatformComponentState()
  packageManager.setComponentEnabledSetting(
    mainActivityAlias,
    newState,
    PackageManager.DONT_KILL_APP,
  )
}

private fun Int.toAppComponentEnabledState(): AppComponentEnabledState {
  return when (this) {
    PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> AppComponentEnabledState.Enabled
    PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> AppComponentEnabledState.Disabled
    else -> AppComponentEnabledState.Unspecified
  }
}

private fun AppComponentEnabledState.toPlatformComponentState(): Int {
  return when (this) {
    AppComponentEnabledState.Enabled -> PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    AppComponentEnabledState.Disabled -> PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    AppComponentEnabledState.Unspecified -> PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
  }
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
