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
import com.github.kr328.clash.settings.ui.AppSettingsUiState
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
      return status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }
    set(value) {
      val status =
        if (value) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        else PackageManager.COMPONENT_ENABLED_STATE_DISABLED

      pm.setComponentEnabledSetting(
        restartReceiverClass.componentName,
        status,
        PackageManager.DONT_KILL_APP,
      )
    }

  private fun hideAppIcon(hide: Boolean) {
    val newState =
      if (hide) {
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED
      } else {
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED
      }
    pm.setComponentEnabledSetting(
      application.mainActivityAlias,
      newState,
      PackageManager.DONT_KILL_APP,
    )
  }
}
