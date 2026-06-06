package com.github.kr328.clash.glue.store

import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.store.Store
import com.github.kr328.clash.common.store.asStoreProvider
import com.github.kr328.clash.common.util.mainActivityAlias
import com.github.kr328.clash.common.util.unsafeLazy
import com.github.kr328.clash.core.model.AccessControlSort
import com.github.kr328.clash.core.model.DarkMode
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.settingsstore.StoreProviderMigration
import com.github.kr328.clash.settingsstore.asSettingsStoreProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn

class UiStore(context: Context) {
  private val preferences =
    context.getSharedPreferences(SETTINGS_PREFERENCE_NAME, Context.MODE_PRIVATE)
  private val store = Store(createStoreProvider(context, preferences))

  val valueState: StateFlow<ValueState> by unsafeLazy {
    val readValues = {
      ValueState(
        enableVpn = enableVpn,
        darkMode = darkMode,
        hideAppIcon = hideAppIcon,
        hideFromRecents = hideFromRecents,
        proxyExcludeNotSelectable = proxyExcludeNotSelectable,
        proxyLine = proxyLine,
        proxySort = proxySort,
        proxyLastGroup = proxyLastGroup,
        accessControlSort = accessControlSort,
        accessControlReverse = accessControlReverse,
        accessControlSystemApp = accessControlSystemApp,
      )
    }
    callbackFlow {
        val listener = OnSharedPreferenceChangeListener { _, _ -> trySend(readValues()) }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(readValues())
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
      }
      .stateIn(
        scope = Global,
        started = SharingStarted.WhileSubscribed(),
        initialValue = readValues(),
      )
  }

  var enableVpn: Boolean by store.boolean(key = "enable_vpn", defaultValue = true)

  var darkMode: DarkMode by
    store.enum(key = "dark_mode", defaultValue = Auto, values = DarkMode.entries.toTypedArray())

  var hideAppIcon: Boolean by
    store.boolean(
      key = "hide_app_icon",
      defaultValue =
        context.packageManager.getComponentEnabledSetting(context.mainActivityAlias).let { state ->
          state != PackageManager.COMPONENT_ENABLED_STATE_ENABLED &&
            state != PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
        },
    )

  var hideFromRecents: Boolean by store.boolean(key = "hide_from_recents", defaultValue = false)

  var proxyExcludeNotSelectable by
    store.boolean(key = "proxy_exclude_not_selectable", defaultValue = false)

  var proxyLine: Int by store.int(key = "proxy_line", defaultValue = 2)

  var proxySort: ProxySort by
    store.enum(
      key = "proxy_sort",
      defaultValue = Default,
      values = ProxySort.entries.toTypedArray(),
    )

  var proxyLastGroup: String by store.string(key = "proxy_last_group", defaultValue = "")

  var accessControlSort: AccessControlSort by
    store.enum(
      key = "access_control_sort",
      defaultValue = AccessControlSort.Label,
      values = AccessControlSort.entries.toTypedArray(),
    )

  var accessControlReverse: Boolean by
    store.boolean(key = "access_control_reverse", defaultValue = false)

  var accessControlSystemApp: Boolean by
    store.boolean(key = "access_control_system_app", defaultValue = false)

  data class ValueState(
    val enableVpn: Boolean,
    val darkMode: DarkMode,
    val hideAppIcon: Boolean,
    val hideFromRecents: Boolean,
    val proxyExcludeNotSelectable: Boolean,
    val proxyLine: Int,
    val proxySort: ProxySort,
    val proxyLastGroup: String,
    val accessControlSort: AccessControlSort,
    val accessControlReverse: Boolean,
    val accessControlSystemApp: Boolean,
  )

  companion object {
    private const val LEGACY_PREFERENCE_NAME = "ui"
    private const val SETTINGS_PREFERENCE_NAME = "settings_ui"
    private const val MIGRATED_KEY = "__migrated_from_shared_preferences_v1"

    private fun createStoreProvider(
      context: Context,
      preferences: android.content.SharedPreferences,
    ) =
      preferences.asSettingsStoreProvider().also { destination ->
        StoreProviderMigration(
            source =
              context
                .getSharedPreferences(LEGACY_PREFERENCE_NAME, Context.MODE_PRIVATE)
                .asStoreProvider(),
            destination = destination,
            migratedKey = MIGRATED_KEY,
          )
          .migrate {
            boolean("enable_vpn", defaultValue = true)
            string("dark_mode", defaultValue = DarkMode.Auto.name)
            boolean("hide_app_icon", defaultValue = false)
            boolean("hide_from_recents", defaultValue = false)
            boolean("proxy_exclude_not_selectable", defaultValue = false)
            int("proxy_line", defaultValue = 2)
            string("proxy_sort", defaultValue = ProxySort.Default.name)
            string("proxy_last_group", defaultValue = "")
            string("access_control_sort", defaultValue = AccessControlSort.Label.name)
            boolean("access_control_reverse", defaultValue = false)
            boolean("access_control_system_app", defaultValue = false)
          }
      }
  }
}
