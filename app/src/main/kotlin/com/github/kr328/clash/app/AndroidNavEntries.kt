package com.github.kr328.clash.app

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.github.kr328.clash.crash.crashEntries
import com.github.kr328.clash.home.HomeRoute
import com.github.kr328.clash.home.homeEntries
import com.github.kr328.clash.log.LogRoute
import com.github.kr328.clash.log.logsEntries
import com.github.kr328.clash.profile.ProfilesRoute
import com.github.kr328.clash.profile.profilesEntries
import com.github.kr328.clash.proxy.ProxyRoute
import com.github.kr328.clash.proxy.proxyEntries
import com.github.kr328.clash.settings.SettingsRoute
import com.github.kr328.clash.settings.settingsEntries
import com.github.kr328.clash.ui.nav.addIfNotLast

internal fun androidEntryProvider(backStack: MutableList<NavKey>): (NavKey) -> NavEntry<NavKey> =
  entryProvider {
    homeEntries(
      onOpenProxy = { backStack.addIfNotLast(ProxyRoute.Proxy) },
      onOpenProfiles = { backStack.addIfNotLast(ProfilesRoute.Profiles()) },
      onOpenProviders = { backStack.addIfNotLast(ProfilesRoute.Providers) },
      onOpenLogs = { backStack.addIfNotLast(LogRoute.Root) },
      onOpenSettings = { backStack.addIfNotLast(SettingsRoute.Root) },
      onOpenHelp = { backStack.addIfNotLast(HomeRoute.Help) },
    )
    proxyEntries(
      onReLaunch = {
        backStack.clear()
        backStack.add(HomeRoute.Home)
      }
    )
    profilesEntries()
    logsEntries()
    settingsEntries()
    crashEntries()
  }
