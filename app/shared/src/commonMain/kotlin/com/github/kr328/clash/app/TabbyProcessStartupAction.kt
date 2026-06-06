package com.github.kr328.clash.app

sealed interface TabbyProcessStartupAction {
  data object StartMainProcess : TabbyProcessStartupAction

  data object NotifyServiceRecreated : TabbyProcessStartupAction
}

fun tabbyProcessStartupAction(
  processName: String,
  packageName: String,
): TabbyProcessStartupAction =
  if (processName == packageName) {
    TabbyProcessStartupAction.StartMainProcess
  } else {
    TabbyProcessStartupAction.NotifyServiceRecreated
  }
