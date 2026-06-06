package com.github.kr328.clash.settings

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.settings.ui.AccessControlScreen
import com.github.kr328.clash.settings.ui.AppSettingsScreen
import com.github.kr328.clash.settings.ui.MetaFeatureSettingsScreen
import com.github.kr328.clash.settings.ui.NetworkSettingsScreen
import com.github.kr328.clash.settings.ui.OverrideSettingsScreen

fun EntryProviderScope<NavKey>.settingsEntries() {
  settingsEntries(
    rootContent = {
      SettingsRouteContent(
        appSettingsContent = { AppSettingsScreen() },
        networkSettingsContent = { onStartAccessControlList ->
          NetworkSettingsScreen(onStartAccessControlList = onStartAccessControlList)
        },
        overrideSettingsContent = { onResetCompleted ->
          OverrideSettingsScreen(onResetCompleted = onResetCompleted)
        },
        metaFeatureSettingsContent = { onResetCompleted ->
          MetaFeatureSettingsScreen(onResetCompleted = onResetCompleted)
        },
        accessControlContent = { AccessControlScreen() },
      )
    }
  )
}
