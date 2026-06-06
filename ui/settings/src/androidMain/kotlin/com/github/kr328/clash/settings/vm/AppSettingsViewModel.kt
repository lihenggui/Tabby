package com.github.kr328.clash.settings.vm

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import com.github.kr328.clash.common.di.AppInfoProvider.Companion.instance as appInfoProvider
import com.github.kr328.clash.common.util.componentName
import com.github.kr328.clash.common.util.mainActivityAlias
import com.github.kr328.clash.core.model.DarkMode
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.store.UiStore
import com.github.kr328.clash.glue.util.ApplicationObserver
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.settings.ui.AppComponentEnabledState
import com.github.kr328.clash.settings.ui.AppSettingsUiState
import com.github.kr328.clash.settings.ui.appSettingsAutoRestartComponentState
import com.github.kr328.clash.settings.ui.appSettingsHideAppIconComponentState
import com.github.kr328.clash.settings.ui.isAppSettingsAutoRestartEnabled
import com.github.kr328.clash.settings.ui.updateAppSettingsAutoRestart
import com.github.kr328.clash.settings.ui.updateAppSettingsDarkMode
import com.github.kr328.clash.settings.ui.updateAppSettingsDynamicNotification
import com.github.kr328.clash.settings.ui.updateAppSettingsHideAppIcon
import com.github.kr328.clash.settings.ui.updateAppSettingsHideFromRecents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

internal class AppSettingsViewModel(app: Application) : AndroidViewModel(app) {
  private val uiStore = UiStore(app)
  private val serviceStore = ServiceStore(app)
  private val pm = app.packageManager
  private val restartReceiverClass = appInfoProvider.restartReceiverClass

  val clashRunning: StateFlow<Boolean> = Remote.broadcasts.clashRunningFlow

  val uiState: StateFlow<AppSettingsUiState>
    field =
      MutableStateFlow(
        AppSettingsUiState(
          autoRestart = autoRestartValue,
          darkMode = uiStore.darkMode,
          hideAppIcon = uiStore.hideAppIcon,
          hideFromRecents = uiStore.hideFromRecents,
          dynamicNotification = serviceStore.dynamicNotification,
        )
      )

  fun updateAutoRestart(value: Boolean) {
    autoRestartValue = value
    uiState.update { updateAppSettingsAutoRestart(it, value) }
  }

  fun updateDarkMode(value: DarkMode) {
    uiStore.darkMode = value
    uiState.update { updateAppSettingsDarkMode(it, value) }
  }

  fun updateHideAppIcon(value: Boolean) {
    hideAppIcon(value)
    uiStore.hideAppIcon = value
    uiState.update { updateAppSettingsHideAppIcon(it, value) }
  }

  fun updateHideFromRecents(value: Boolean) {
    ApplicationObserver.createdActivities.forEach { it.recreate() }
    uiStore.hideFromRecents = value
    uiState.update { updateAppSettingsHideFromRecents(it, value) }
  }

  fun updateDynamicNotification(value: Boolean) {
    serviceStore.dynamicNotification = value
    uiState.update { updateAppSettingsDynamicNotification(it, value) }
  }

  private var autoRestartValue: Boolean
    get() {
      val status = pm.getComponentEnabledSetting(restartReceiverClass.componentName)
      return isAppSettingsAutoRestartEnabled(status.toAppComponentEnabledState())
    }
    set(value) {
      val status = appSettingsAutoRestartComponentState(value).toPackageManagerComponentState()

      pm.setComponentEnabledSetting(
        restartReceiverClass.componentName,
        status,
        PackageManager.DONT_KILL_APP,
      )
    }

  private fun hideAppIcon(hide: Boolean) {
    val newState = appSettingsHideAppIconComponentState(hide).toPackageManagerComponentState()
    pm.setComponentEnabledSetting(
      application.mainActivityAlias,
      newState,
      PackageManager.DONT_KILL_APP,
    )
  }
}

private fun Int.toAppComponentEnabledState(): AppComponentEnabledState {
  return when (this) {
    PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> AppComponentEnabledState.Enabled
    PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> AppComponentEnabledState.Disabled
    else -> AppComponentEnabledState.Unspecified
  }
}

private fun AppComponentEnabledState.toPackageManagerComponentState(): Int {
  return when (this) {
    AppComponentEnabledState.Enabled -> PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    AppComponentEnabledState.Disabled -> PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    AppComponentEnabledState.Unspecified -> PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
  }
}
