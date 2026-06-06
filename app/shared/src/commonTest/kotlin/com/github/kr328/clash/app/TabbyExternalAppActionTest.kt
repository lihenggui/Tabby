package com.github.kr328.clash.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class TabbyExternalAppActionTest {
  @Test
  fun tabbyExternalAppActionPlanInstallsProfileWhenRequestIsAvailable() {
    assertEquals(
      TabbyExternalAppActionPlan.InstallProfile,
      tabbyExternalAppActionPlan(TabbyExternalAppAction.InstallProfile(requestAvailable = true)),
    )
  }

  @Test
  fun tabbyExternalAppActionPlanIgnoresMissingInstallProfileRequest() {
    assertEquals(
      TabbyExternalAppActionPlan.Ignore,
      tabbyExternalAppActionPlan(TabbyExternalAppAction.InstallProfile(requestAvailable = false)),
    )
  }

  @Test
  fun tabbyExternalAppActionPlanOpensProfilePropertiesWhenUuidIsAvailable() {
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")

    assertEquals(
      TabbyExternalAppActionPlan.OpenRoute(TabbyExternalRouteAction.OpenProfileProperties(uuid)),
      tabbyExternalAppActionPlan(TabbyExternalAppAction.OpenProfileProperties(uuid)),
    )
  }

  @Test
  fun tabbyExternalAppActionPlanIgnoresMissingProfilePropertiesUuid() {
    assertEquals(
      TabbyExternalAppActionPlan.Ignore,
      tabbyExternalAppActionPlan(TabbyExternalAppAction.OpenProfileProperties(uuid = null)),
    )
  }

  @Test
  fun tabbyExternalAppActionPlanMapsLogAndCrashRoutes() {
    assertEquals(
      TabbyExternalAppActionPlan.OpenRoute(TabbyExternalRouteAction.OpenLogs),
      tabbyExternalAppActionPlan(TabbyExternalAppAction.OpenLogs),
    )
    assertEquals(
      TabbyExternalAppActionPlan.OpenRoute(TabbyExternalRouteAction.OpenAppCrashed),
      tabbyExternalAppActionPlan(TabbyExternalAppAction.OpenAppCrashed),
    )
    assertEquals(
      TabbyExternalAppActionPlan.OpenRoute(TabbyExternalRouteAction.OpenApkBroken),
      tabbyExternalAppActionPlan(TabbyExternalAppAction.OpenApkBroken),
    )
  }
}
