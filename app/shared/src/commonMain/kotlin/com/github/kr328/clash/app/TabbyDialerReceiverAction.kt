package com.github.kr328.clash.app

sealed interface TabbyDialerReceiverAction {
  data object OpenMainActivity : TabbyDialerReceiverAction
}

data class TabbyDialerReceiverMainActivityLaunchOptions(val openInNewTask: Boolean)

fun tabbyDialerReceiverAction(): TabbyDialerReceiverAction =
  TabbyDialerReceiverAction.OpenMainActivity

fun tabbyDialerReceiverMainActivityLaunchOptions(
  action: TabbyDialerReceiverAction
): TabbyDialerReceiverMainActivityLaunchOptions =
  when (action) {
    TabbyDialerReceiverAction.OpenMainActivity ->
      TabbyDialerReceiverMainActivityLaunchOptions(openInNewTask = true)
  }

fun tabbyDialerReceiverMainActivityLaunchFlags(
  launchOptions: TabbyDialerReceiverMainActivityLaunchOptions,
  openInNewTaskFlag: Int,
): Int {
  var flags = 0
  if (launchOptions.openInNewTask) flags = flags or openInNewTaskFlag
  return flags
}
