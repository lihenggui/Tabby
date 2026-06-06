package com.github.kr328.clash.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class TabbyExternalAppActionTest {
  @Test
  fun tabbyExternalAppActionFromStringParsesInstallProfileAction() {
    assertEquals(
      TabbyExternalAppAction.InstallProfile(requestAvailable = true),
      tabbyExternalAppActionFromString(
        action = "view",
        requestAvailable = true,
        profilePropertiesUuid = null,
        installProfileAction = "view",
        profilePropertiesAction = "properties",
        logsAction = "logs",
        appCrashedAction = "crashed",
        apkBrokenAction = "apk-broken",
      ),
    )
  }

  @Test
  fun tabbyExternalAppActionFromStringParsesProfilePropertiesAction() {
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000002")

    assertEquals(
      TabbyExternalAppAction.OpenProfileProperties(uuid),
      tabbyExternalAppActionFromString(
        action = "properties",
        requestAvailable = false,
        profilePropertiesUuid = uuid,
        installProfileAction = "view",
        profilePropertiesAction = "properties",
        logsAction = "logs",
        appCrashedAction = "crashed",
        apkBrokenAction = "apk-broken",
      ),
    )
  }

  @Test
  fun tabbyExternalAppActionFromStringParsesRouteActions() {
    assertEquals(
      TabbyExternalAppAction.OpenLogs,
      tabbyExternalAppActionFromString(
        action = "logs",
        requestAvailable = false,
        profilePropertiesUuid = null,
        installProfileAction = "view",
        profilePropertiesAction = "properties",
        logsAction = "logs",
        appCrashedAction = "crashed",
        apkBrokenAction = "apk-broken",
      ),
    )
    assertEquals(
      TabbyExternalAppAction.OpenAppCrashed,
      tabbyExternalAppActionFromString(
        action = "crashed",
        requestAvailable = false,
        profilePropertiesUuid = null,
        installProfileAction = "view",
        profilePropertiesAction = "properties",
        logsAction = "logs",
        appCrashedAction = "crashed",
        apkBrokenAction = "apk-broken",
      ),
    )
    assertEquals(
      TabbyExternalAppAction.OpenApkBroken,
      tabbyExternalAppActionFromString(
        action = "apk-broken",
        requestAvailable = false,
        profilePropertiesUuid = null,
        installProfileAction = "view",
        profilePropertiesAction = "properties",
        logsAction = "logs",
        appCrashedAction = "crashed",
        apkBrokenAction = "apk-broken",
      ),
    )
  }

  @Test
  fun tabbyExternalAppActionFromStringIgnoresUnknownActions() {
    assertEquals(
      null,
      tabbyExternalAppActionFromString(
        action = "unknown",
        requestAvailable = true,
        profilePropertiesUuid = Uuid.parse("00000000-0000-0000-0000-000000000003"),
        installProfileAction = "view",
        profilePropertiesAction = "properties",
        logsAction = "logs",
        appCrashedAction = "crashed",
        apkBrokenAction = "apk-broken",
      ),
    )
  }

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
  fun tabbyExternalAppActionPlanIgnoresUnknownActions() {
    assertEquals(
      TabbyExternalAppActionPlan.Ignore,
      tabbyExternalAppActionPlan(null),
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
