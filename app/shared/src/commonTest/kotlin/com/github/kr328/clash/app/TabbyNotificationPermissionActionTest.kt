package com.github.kr328.clash.app

import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyNotificationPermissionActionTest {
  @Test
  fun tabbyNotificationPermissionActionRequestsWhenRuntimePermissionIsRequiredAndMissing() {
    assertEquals(
      TabbyNotificationPermissionAction.RequestNotificationPermission,
      tabbyNotificationPermissionAction(
        runtimePermissionRequired = true,
        permissionGranted = false,
      ),
    )
  }

  @Test
  fun tabbyNotificationPermissionActionIgnoresWhenRuntimePermissionIsNotRequired() {
    assertEquals(
      TabbyNotificationPermissionAction.Ignore,
      tabbyNotificationPermissionAction(
        runtimePermissionRequired = false,
        permissionGranted = false,
      ),
    )
  }

  @Test
  fun tabbyNotificationPermissionActionIgnoresWhenPermissionIsAlreadyGranted() {
    assertEquals(
      TabbyNotificationPermissionAction.Ignore,
      tabbyNotificationPermissionAction(
        runtimePermissionRequired = true,
        permissionGranted = true,
      ),
    )
  }

  @Test
  fun tabbyNotificationPermissionPlatformStateRequestsAtRuntimePermissionSdkWhenMissing() {
    assertEquals(
      TabbyNotificationPermissionAction.RequestNotificationPermission,
      tabbyNotificationPermissionActionFromPlatformState(
        platformSdk = 33,
        runtimePermissionMinimumPlatformSdk = 33,
        permissionGranted = false,
      ),
    )
  }

  @Test
  fun tabbyNotificationPermissionPlatformStateIgnoresBelowRuntimePermissionSdk() {
    assertEquals(
      TabbyNotificationPermissionAction.Ignore,
      tabbyNotificationPermissionActionFromPlatformState(
        platformSdk = 32,
        runtimePermissionMinimumPlatformSdk = 33,
        permissionGranted = false,
      ),
    )
  }
}
