package com.github.kr328.clash.common.service

enum class TabbyModuleBroadcastReceiveAction {
  Deliver,
  CloseChannel,
}

fun tabbyModuleBroadcastReceiveAction(
  hasReceiver: Boolean,
  hasPayload: Boolean,
): TabbyModuleBroadcastReceiveAction {
  return if (hasReceiver && hasPayload) {
    TabbyModuleBroadcastReceiveAction.Deliver
  } else {
    TabbyModuleBroadcastReceiveAction.CloseChannel
  }
}
