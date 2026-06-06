package com.github.kr328.clash.home

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.home.ui.HelpScreen
import com.github.kr328.clash.home.ui.HomeScreen

fun EntryProviderScope<NavKey>.homeEntries(
  onOpenProxy: () -> Unit,
  onOpenProfiles: () -> Unit,
  onOpenProviders: () -> Unit,
  onOpenLogs: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenHelp: () -> Unit,
) {
  homeEntries(
    homeContent = {
      HomeScreen(
        onOpenProxy = onOpenProxy,
        onOpenProfiles = onOpenProfiles,
        onOpenProviders = onOpenProviders,
        onOpenLogs = onOpenLogs,
        onOpenSettings = onOpenSettings,
        onOpenHelp = onOpenHelp,
      )
    },
    helpContent = { HelpScreen() },
  )
}
