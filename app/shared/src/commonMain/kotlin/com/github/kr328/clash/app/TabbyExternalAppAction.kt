package com.github.kr328.clash.app

import kotlin.uuid.Uuid

sealed interface TabbyExternalAppAction {
  data class InstallProfile(val requestAvailable: Boolean) : TabbyExternalAppAction

  data class OpenProfileProperties(val uuid: Uuid?) : TabbyExternalAppAction

  data object OpenLogs : TabbyExternalAppAction

  data object OpenAppCrashed : TabbyExternalAppAction

  data object OpenApkBroken : TabbyExternalAppAction
}

sealed interface TabbyExternalAppActionPlan {
  data object InstallProfile : TabbyExternalAppActionPlan

  data class OpenRoute(val routeAction: TabbyExternalRouteAction) : TabbyExternalAppActionPlan

  data object Ignore : TabbyExternalAppActionPlan
}

fun tabbyExternalAppActionPlan(action: TabbyExternalAppAction?): TabbyExternalAppActionPlan =
  when (action) {
    is TabbyExternalAppAction.InstallProfile ->
      if (action.requestAvailable) {
        TabbyExternalAppActionPlan.InstallProfile
      } else {
        TabbyExternalAppActionPlan.Ignore
      }
    is TabbyExternalAppAction.OpenProfileProperties ->
      action.uuid?.let {
        TabbyExternalAppActionPlan.OpenRoute(TabbyExternalRouteAction.OpenProfileProperties(it))
      } ?: TabbyExternalAppActionPlan.Ignore
    TabbyExternalAppAction.OpenLogs ->
      TabbyExternalAppActionPlan.OpenRoute(TabbyExternalRouteAction.OpenLogs)
    TabbyExternalAppAction.OpenAppCrashed ->
      TabbyExternalAppActionPlan.OpenRoute(TabbyExternalRouteAction.OpenAppCrashed)
    TabbyExternalAppAction.OpenApkBroken ->
      TabbyExternalAppActionPlan.OpenRoute(TabbyExternalRouteAction.OpenApkBroken)
    null -> TabbyExternalAppActionPlan.Ignore
  }
