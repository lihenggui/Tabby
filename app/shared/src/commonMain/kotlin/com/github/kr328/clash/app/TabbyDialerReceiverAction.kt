package com.github.kr328.clash.app

sealed interface TabbyDialerReceiverAction {
  data object OpenMainActivity : TabbyDialerReceiverAction
}

fun tabbyDialerReceiverAction(): TabbyDialerReceiverAction =
  TabbyDialerReceiverAction.OpenMainActivity
