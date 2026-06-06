package com.github.kr328.clash.app

import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyProcessStartupActionTest {
  @Test
  fun tabbyProcessStartupActionStartsMainProcessForMainProcess() {
    assertEquals(
      TabbyProcessStartupAction.StartMainProcess,
      tabbyProcessStartupAction(
        processName = "io.github.goooler.tabby",
        packageName = "io.github.goooler.tabby",
      ),
    )
  }

  @Test
  fun tabbyProcessStartupActionNotifiesServiceRecreatedForOtherProcesses() {
    assertEquals(
      TabbyProcessStartupAction.NotifyServiceRecreated,
      tabbyProcessStartupAction(
        processName = "io.github.goooler.tabby:service",
        packageName = "io.github.goooler.tabby",
      ),
    )
  }
}
