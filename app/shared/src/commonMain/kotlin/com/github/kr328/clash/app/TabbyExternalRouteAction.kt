package com.github.kr328.clash.app

import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.crash.CrashRoute
import com.github.kr328.clash.log.LogRoute
import com.github.kr328.clash.profile.ProfilesRoute
import com.github.kr328.clash.ui.nav.addIfNotLast
import kotlin.uuid.Uuid

sealed interface TabbyExternalRouteAction {
  data class OpenProfileProperties(val uuid: Uuid) : TabbyExternalRouteAction

  data object OpenLogs : TabbyExternalRouteAction

  data object OpenAppCrashed : TabbyExternalRouteAction

  data object OpenApkBroken : TabbyExternalRouteAction
}

fun MutableList<NavKey>.handleTabbyExternalRouteAction(action: TabbyExternalRouteAction) {
  when (action) {
    is TabbyExternalRouteAction.OpenProfileProperties ->
      addIfNotLast(ProfilesRoute.Profiles(openPropertyUuid = action.uuid))
    TabbyExternalRouteAction.OpenLogs -> addIfNotLast(LogRoute.Root)
    TabbyExternalRouteAction.OpenAppCrashed -> replaceWith(CrashRoute.AppCrashed)
    TabbyExternalRouteAction.OpenApkBroken -> replaceWith(CrashRoute.ApkBroken)
  }
}
