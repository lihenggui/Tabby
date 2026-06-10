package com.github.kr328.clash.app

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.crash.crashEntries
import com.github.kr328.clash.home.homeEntries
import com.github.kr328.clash.log.logsEntries
import com.github.kr328.clash.profile.profilesEntries
import com.github.kr328.clash.proxy.proxyEntries
import com.github.kr328.clash.settings.settingsEntries

internal fun androidEntryProvider(backStack: MutableList<NavKey>): (NavKey) -> NavEntry<NavKey> =
  tabbyEntryProvider(
    backStack = backStack,
    homeEntries = { actions ->
      homeEntries(
        onOpenProxy = actions.openProxy,
        onOpenProfiles = actions.openProfiles,
        onOpenProviders = actions.openProviders,
        onOpenLogs = actions.openLogs,
        onOpenSettings = actions.openSettings,
        onOpenHelp = actions.openHelp,
      )
    },
    proxyEntries = { onReLaunch -> proxyEntries(onReLaunch = onReLaunch) },
    profilesEntries = { profilesEntries() },
    logsEntries = { logsEntries() },
    settingsEntries = { settingsEntries() },
    crashEntries = { crashEntries() },
  )
