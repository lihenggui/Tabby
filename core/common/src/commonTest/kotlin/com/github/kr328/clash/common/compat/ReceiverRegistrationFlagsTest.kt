package com.github.kr328.clash.common.compat

import kotlin.test.Test
import kotlin.test.assertEquals

class ReceiverRegistrationFlagsTest {
  @Test
  fun exportsReceiverWhenNoPermissionIsRequired() {
    assertEquals(
      EXPORTED_FLAG,
      tabbyReceiverRegistrationFlags(
        permission = null,
        exportedFlag = EXPORTED_FLAG,
        notExportedFlag = NOT_EXPORTED_FLAG,
      ),
    )
  }

  @Test
  fun keepsReceiverNotExportedWhenPermissionIsRequired() {
    assertEquals(
      NOT_EXPORTED_FLAG,
      tabbyReceiverRegistrationFlags(
        permission = "io.github.goooler.tabby.permission.RECEIVE_BROADCASTS",
        exportedFlag = EXPORTED_FLAG,
        notExportedFlag = NOT_EXPORTED_FLAG,
      ),
    )
  }
}

private const val EXPORTED_FLAG = 0b01
private const val NOT_EXPORTED_FLAG = 0b10
