package com.github.kr328.clash.common.service

import kotlin.test.Test
import kotlin.test.assertEquals

class ModuleBroadcastReceiveActionTest {
  @Test
  fun deliversBroadcastWhenReceiverAndPayloadArePresent() {
    assertEquals(
      TabbyModuleBroadcastReceiveAction.Deliver,
      tabbyModuleBroadcastReceiveAction(hasReceiver = true, hasPayload = true),
    )
  }

  @Test
  fun closesBroadcastChannelWhenReceiverIsMissing() {
    assertEquals(
      TabbyModuleBroadcastReceiveAction.CloseChannel,
      tabbyModuleBroadcastReceiveAction(hasReceiver = false, hasPayload = true),
    )
  }

  @Test
  fun closesBroadcastChannelWhenPayloadIsMissing() {
    assertEquals(
      TabbyModuleBroadcastReceiveAction.CloseChannel,
      tabbyModuleBroadcastReceiveAction(hasReceiver = true, hasPayload = false),
    )
  }

  @Test
  fun closesBroadcastChannelWhenReceiverAndPayloadAreMissing() {
    assertEquals(
      TabbyModuleBroadcastReceiveAction.CloseChannel,
      tabbyModuleBroadcastReceiveAction(hasReceiver = false, hasPayload = false),
    )
  }
}
