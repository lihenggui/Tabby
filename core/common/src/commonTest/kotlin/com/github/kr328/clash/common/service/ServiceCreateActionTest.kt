package com.github.kr328.clash.common.service

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceCreateActionTest {
  @Test
  fun stopsDuplicateServiceWhenAlreadyRunning() {
    assertEquals(
      TabbyServiceCreateAction.StopDuplicate,
      tabbyServiceCreateAction(serviceRunning = true),
    )
  }

  @Test
  fun startsServiceWhenNoServiceIsRunning() {
    assertEquals(
      TabbyServiceCreateAction.StartService,
      tabbyServiceCreateAction(serviceRunning = false),
    )
  }
}
