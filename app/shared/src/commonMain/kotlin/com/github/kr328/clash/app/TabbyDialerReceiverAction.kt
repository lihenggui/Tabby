package com.github.kr328.clash.app

sealed interface TabbyDialerReceiverAction {
  data object OpenMainActivity : TabbyDialerReceiverAction
}

data class TabbyDialerReceiverPlatformSpec(val intentFlags: Int)

fun tabbyDialerReceiverAction(): TabbyDialerReceiverAction =
  TabbyDialerReceiverAction.OpenMainActivity

fun tabbyDialerReceiverPlatformSpec(
  action: TabbyDialerReceiverAction,
  openInNewTaskFlag: Int,
): TabbyDialerReceiverPlatformSpec =
  when (action) {
    TabbyDialerReceiverAction.OpenMainActivity ->
      TabbyDialerReceiverPlatformSpec(intentFlags = openInNewTaskFlag)
  }
