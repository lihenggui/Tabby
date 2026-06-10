package com.github.kr328.clash.common.network

import kotlin.test.Test
import kotlin.test.assertEquals

class TunConnectionOwnerUidTest {
  @Test
  fun disablesConnectionOwnerUidLookupBeforeApi29() {
    assertEquals(
      false,
      tabbyTunCanQueryConnectionOwnerUid(TABBY_TUN_CONNECTION_OWNER_UID_MIN_SDK - 1),
    )
  }

  @Test
  fun enablesConnectionOwnerUidLookupFromApi29() {
    assertEquals(
      true,
      tabbyTunCanQueryConnectionOwnerUid(TABBY_TUN_CONNECTION_OWNER_UID_MIN_SDK),
    )
  }
}
