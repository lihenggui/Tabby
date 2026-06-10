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
import kotlin.uuid.Uuid

class TabbyNavigationActionsTest {
  @Test
  fun tabbyInitialBackStackStartsAtHomeRoute() {
    val backStack = tabbyInitialBackStack()

    assertEquals(listOf<NavKey>(HomeRoute.Home), backStack)

    backStack.add(ProxyRoute.Proxy)
    assertEquals(listOf<NavKey>(HomeRoute.Home, ProxyRoute.Proxy), backStack)
  }

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

  @Test
  fun tabbyExternalRouteActionsOpenAndroidEntryRoutesFromCommonHandler() {
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")
    val backStack = mutableListOf<NavKey>(HomeRoute.Home)

    backStack.handleTabbyExternalRouteAction(TabbyExternalRouteAction.OpenProfileProperties(uuid))
    backStack.handleTabbyExternalRouteAction(TabbyExternalRouteAction.OpenProfileProperties(uuid))
    backStack.handleTabbyExternalRouteAction(TabbyExternalRouteAction.OpenLogs)
    backStack.handleTabbyExternalRouteAction(TabbyExternalRouteAction.OpenLogs)

    assertEquals(
      listOf<NavKey>(
        HomeRoute.Home,
        ProfilesRoute.Profiles(openPropertyUuid = uuid),
        LogRoute.Root,
      ),
      backStack,
    )
  }

  @Test
  fun tabbyExternalRouteActionsReplaceStackForCrashRoutes() {
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")
    val backStack =
      mutableListOf<NavKey>(
        HomeRoute.Home,
        ProfilesRoute.Profiles(openPropertyUuid = uuid),
        LogRoute.Root,
      )

    backStack.handleTabbyExternalRouteAction(TabbyExternalRouteAction.OpenAppCrashed)
    assertEquals(listOf<NavKey>(CrashRoute.AppCrashed), backStack)

    backStack.handleTabbyExternalRouteAction(TabbyExternalRouteAction.OpenApkBroken)
    assertEquals(listOf<NavKey>(CrashRoute.ApkBroken), backStack)
  }
}
