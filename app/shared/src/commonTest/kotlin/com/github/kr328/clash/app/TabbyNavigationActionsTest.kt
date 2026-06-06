package com.github.kr328.clash.app

import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.crash.CrashRoute
import com.github.kr328.clash.home.HomeRoute
import com.github.kr328.clash.log.LogRoute
import com.github.kr328.clash.profile.ProfilesRoute
import com.github.kr328.clash.proxy.ProxyRoute
import com.github.kr328.clash.settings.SettingsRoute
import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyNavigationActionsTest {
  @Test
  fun tabbyNavigationActionsAppendFeatureRoutesWithoutDuplicatingLastRoute() {
    val backStack = mutableListOf<NavKey>(HomeRoute.Home)
    val actions = tabbyNavigationActions(backStack)

    actions.openProxy()
    actions.openProxy()
    actions.openProfiles()
    actions.openProviders()
    actions.openLogs()
    actions.openSettings()
    actions.openHelp()

    assertEquals(
      listOf<NavKey>(
        HomeRoute.Home,
        ProxyRoute.Proxy,
        ProfilesRoute.Profiles(),
        ProfilesRoute.Providers,
        LogRoute.Root,
        SettingsRoute.Root,
        HomeRoute.Help,
      ),
      backStack,
    )
  }

  @Test
  fun tabbyNavigationActionsReplaceStackForCrashAndRelaunchRoutes() {
    val backStack = mutableListOf<NavKey>(HomeRoute.Home, ProxyRoute.Proxy)
    val actions = tabbyNavigationActions(backStack)

    actions.openAppCrashed()
    assertEquals(listOf<NavKey>(CrashRoute.AppCrashed), backStack)

    actions.openApkBroken()
    assertEquals(listOf<NavKey>(CrashRoute.ApkBroken), backStack)

    actions.relaunchHome()
    assertEquals(listOf<NavKey>(HomeRoute.Home), backStack)
  }
}
