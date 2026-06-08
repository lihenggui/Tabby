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
