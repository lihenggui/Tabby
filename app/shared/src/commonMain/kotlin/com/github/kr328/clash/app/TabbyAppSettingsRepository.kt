package com.github.kr328.clash.app

import com.github.kr328.clash.common.store.Store
import com.github.kr328.clash.common.store.StoreProvider
import com.github.kr328.clash.core.model.DarkMode

data class TabbyAppSettings(
  val autoRestart: Boolean = false,
  val darkMode: DarkMode = DarkMode.Auto,
  val hideAppIcon: Boolean = false,
  val hideFromRecents: Boolean = false,
  val dynamicNotification: Boolean = true,
)

class TabbyAppSettingsRepository(
  private val uiStoreProvider: StoreProvider,
  private val serviceStoreProvider: StoreProvider,
) {
  private val uiStore = Store(uiStoreProvider)
  private val serviceStore = Store(serviceStoreProvider)

  private var storedAutoRestart by uiStore.boolean(AUTO_RESTART_KEY, false)
  private var storedDarkMode by
    uiStore.enum(DARK_MODE_KEY, DarkMode.Auto, DarkMode.entries.toTypedArray())
  private var storedHideAppIcon by uiStore.boolean(HIDE_APP_ICON_KEY, false)
  private var storedHideFromRecents by uiStore.boolean(HIDE_FROM_RECENTS_KEY, false)
  private var storedDynamicNotification by serviceStore.boolean(DYNAMIC_NOTIFICATION_KEY, true)

  val hasDarkMode: Boolean
    get() = uiStoreProvider.contains(DARK_MODE_KEY)

  fun query(defaults: TabbyAppSettings = TabbyAppSettings()): TabbyAppSettings {
    return TabbyAppSettings(
      autoRestart =
        if (uiStoreProvider.contains(AUTO_RESTART_KEY)) storedAutoRestart else defaults.autoRestart,
      darkMode = if (hasDarkMode) storedDarkMode else defaults.darkMode,
      hideAppIcon =
        if (uiStoreProvider.contains(HIDE_APP_ICON_KEY)) storedHideAppIcon
        else defaults.hideAppIcon,
      hideFromRecents =
        if (uiStoreProvider.contains(HIDE_FROM_RECENTS_KEY)) storedHideFromRecents
        else defaults.hideFromRecents,
      dynamicNotification =
        if (serviceStoreProvider.contains(DYNAMIC_NOTIFICATION_KEY)) storedDynamicNotification
        else defaults.dynamicNotification,
    )
  }

  fun setAutoRestart(value: Boolean) {
    storedAutoRestart = value
  }

  fun setDarkMode(value: DarkMode) {
    storedDarkMode = value
  }

  fun setHideAppIcon(value: Boolean) {
    storedHideAppIcon = value
  }

  fun setHideFromRecents(value: Boolean) {
    storedHideFromRecents = value
  }

  fun setDynamicNotification(value: Boolean) {
    storedDynamicNotification = value
  }

  private companion object {
    const val AUTO_RESTART_KEY = "auto_restart"
    const val DARK_MODE_KEY = "dark_mode"
    const val HIDE_APP_ICON_KEY = "hide_app_icon"
    const val HIDE_FROM_RECENTS_KEY = "hide_from_recents"
    const val DYNAMIC_NOTIFICATION_KEY = "dynamic_notification"
  }
}
