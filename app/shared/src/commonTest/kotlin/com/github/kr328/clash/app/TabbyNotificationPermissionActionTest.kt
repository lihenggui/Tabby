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
  fun tabbyNotificationRuntimePermissionRequiredFromPlatformSdkUsesMinimumSdkVersion() {
    assertEquals(
      false,
      tabbyNotificationRuntimePermissionRequiredFromPlatformSdk(
        sdkVersion = 32,
        runtimePermissionSdkVersion = 33,
      ),
    )
    assertEquals(
      true,
      tabbyNotificationRuntimePermissionRequiredFromPlatformSdk(
        sdkVersion = 33,
        runtimePermissionSdkVersion = 33,
      ),
    )
    assertEquals(
      true,
      tabbyNotificationRuntimePermissionRequiredFromPlatformSdk(
        sdkVersion = 34,
        runtimePermissionSdkVersion = 33,
      ),
    )
  }

  @Test
  fun tabbyNotificationPermissionGrantedFromPlatformResultMapsGrantedResultOnly() {
    assertEquals(
      true,
      tabbyNotificationPermissionGrantedFromPlatformResult(
        permissionResult = 0,
        grantedResult = 0,
      ),
    )
    assertEquals(
      false,
      tabbyNotificationPermissionGrantedFromPlatformResult(
        permissionResult = -1,
        grantedResult = 0,
      ),
    )
  }

  @Test
  fun tabbyNotificationPermissionActionFromPlatformStateMapsPlatformInputsToAction() {
    assertEquals(
      TabbyNotificationPermissionAction.Ignore,
      tabbyNotificationPermissionActionFromPlatformState(
        sdkVersion = 32,
        runtimePermissionSdkVersion = 33,
        permissionResult = -1,
        grantedResult = 0,
      ),
    )
    assertEquals(
      TabbyNotificationPermissionAction.Ignore,
      tabbyNotificationPermissionActionFromPlatformState(
        sdkVersion = 33,
        runtimePermissionSdkVersion = 33,
        permissionResult = 0,
        grantedResult = 0,
      ),
    )
    assertEquals(
      TabbyNotificationPermissionAction.RequestNotificationPermission,
      tabbyNotificationPermissionActionFromPlatformState(
        sdkVersion = 33,
        runtimePermissionSdkVersion = 33,
        permissionResult = -1,
        grantedResult = 0,
      ),
    )
  }
}
