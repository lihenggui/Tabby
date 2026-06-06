package com.github.kr328.clash.app

sealed interface TabbyNotificationPermissionAction {
  data object RequestNotificationPermission : TabbyNotificationPermissionAction

  data object Ignore : TabbyNotificationPermissionAction
}

fun tabbyNotificationPermissionAction(
  runtimePermissionRequired: Boolean,
  permissionGranted: Boolean,
): TabbyNotificationPermissionAction =
  if (runtimePermissionRequired && !permissionGranted) {
    TabbyNotificationPermissionAction.RequestNotificationPermission
  } else {
    TabbyNotificationPermissionAction.Ignore
  }

fun tabbyNotificationRuntimePermissionRequiredFromPlatformSdk(
  sdkVersion: Int,
  runtimePermissionSdkVersion: Int,
): Boolean {
  return sdkVersion >= runtimePermissionSdkVersion
}

fun tabbyNotificationPermissionGrantedFromPlatformResult(
  permissionResult: Int,
  grantedResult: Int,
): Boolean {
  return permissionResult == grantedResult
}

fun tabbyNotificationPermissionActionFromPlatformState(
  sdkVersion: Int,
  runtimePermissionSdkVersion: Int,
  permissionResult: Int,
  grantedResult: Int,
): TabbyNotificationPermissionAction {
  return tabbyNotificationPermissionAction(
    runtimePermissionRequired =
      tabbyNotificationRuntimePermissionRequiredFromPlatformSdk(
        sdkVersion = sdkVersion,
        runtimePermissionSdkVersion = runtimePermissionSdkVersion,
      ),
    permissionGranted =
      tabbyNotificationPermissionGrantedFromPlatformResult(
        permissionResult = permissionResult,
        grantedResult = grantedResult,
      ),
  )
}
