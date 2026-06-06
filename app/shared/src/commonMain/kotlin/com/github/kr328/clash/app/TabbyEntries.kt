package com.github.kr328.clash.app

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.github.kr328.clash.crash.CrashRoute
import com.github.kr328.clash.home.HomeRoute
import com.github.kr328.clash.log.LogRoute
import com.github.kr328.clash.profile.ProfilesRoute
import com.github.kr328.clash.proxy.ProxyRoute
import com.github.kr328.clash.settings.SettingsRoute
import com.github.kr328.clash.ui.nav.addIfNotLast

class TabbyNavigationActions
internal constructor(
  val openProxy: () -> Unit,
  val openProfiles: () -> Unit,
  val openProviders: () -> Unit,
  val openLogs: () -> Unit,
  val openSettings: () -> Unit,
  val openHelp: () -> Unit,
  val openAppCrashed: () -> Unit,
  val openApkBroken: () -> Unit,
  val relaunchHome: () -> Unit,
)

fun tabbyEntryProvider(
  backStack: MutableList<NavKey>,
  homeEntries: EntryProviderScope<NavKey>.(TabbyNavigationActions) -> Unit,
  proxyEntries: EntryProviderScope<NavKey>.(onReLaunch: () -> Unit) -> Unit,
  profilesEntries: EntryProviderScope<NavKey>.() -> Unit,
  logsEntries: EntryProviderScope<NavKey>.() -> Unit,
  settingsEntries: EntryProviderScope<NavKey>.() -> Unit,
  crashEntries: EntryProviderScope<NavKey>.() -> Unit,
): (NavKey) -> NavEntry<NavKey> {
  val actions = tabbyNavigationActions(backStack)
  return entryProvider<NavKey> {
    homeEntries(actions)
    proxyEntries(actions.relaunchHome)
    profilesEntries()
    logsEntries()
    settingsEntries()
    crashEntries()
  }
}

internal fun tabbyNavigationActions(backStack: MutableList<NavKey>): TabbyNavigationActions =
  TabbyNavigationActions(
    openProxy = { backStack.addIfNotLast(ProxyRoute.Proxy) },
    openProfiles = { backStack.addIfNotLast(ProfilesRoute.Profiles()) },
    openProviders = { backStack.addIfNotLast(ProfilesRoute.Providers) },
    openLogs = { backStack.addIfNotLast(LogRoute.Root) },
    openSettings = { backStack.addIfNotLast(SettingsRoute.Root) },
    openHelp = { backStack.addIfNotLast(HomeRoute.Help) },
    openAppCrashed = { backStack.replaceWith(CrashRoute.AppCrashed) },
    openApkBroken = { backStack.replaceWith(CrashRoute.ApkBroken) },
    relaunchHome = { backStack.replaceWith(HomeRoute.Home) },
  )

internal fun MutableList<NavKey>.replaceWith(route: NavKey) {
  clear()
  add(route)
}
