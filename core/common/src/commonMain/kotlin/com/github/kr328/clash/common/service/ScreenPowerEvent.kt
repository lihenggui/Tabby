package com.github.kr328.clash.common.service

enum class TabbyScreenPowerEvent(val isInteractive: Boolean) {
  ScreenOn(isInteractive = true),
  ScreenOff(isInteractive = false),
}

fun tabbyScreenPowerEventFromPlatformAction(
  action: String?,
  screenOnAction: String,
  screenOffAction: String,
): TabbyScreenPowerEvent? {
  return when (action) {
    screenOnAction -> TabbyScreenPowerEvent.ScreenOn
    screenOffAction -> TabbyScreenPowerEvent.ScreenOff
    else -> null
  }
}

fun tabbyShouldSuspendCoreForInteractiveState(isInteractive: Boolean): Boolean {
  return !isInteractive
}
