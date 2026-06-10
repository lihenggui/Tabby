package com.github.kr328.clash.common.compat

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingIntentFlagsTest {
  @Test
  fun keepsImmutableFlagWhenMutableIsNotRequested() {
    assertEquals(
      0b101,
      tabbyPendingIntentFlagsFromPlatformState(
        flags = 0b001,
        mutable = false,
        platformSdk = TABBY_PENDING_INTENT_MUTABLE_MIN_SDK,
        mutableFlag = 0b010,
        immutableFlag = 0b100,
      ),
    )
  }

  @Test
  fun keepsImmutableFlagBeforeMutableFlagIsAvailable() {
    assertEquals(
      0b101,
      tabbyPendingIntentFlagsFromPlatformState(
        flags = 0b001,
        mutable = true,
        platformSdk = TABBY_PENDING_INTENT_MUTABLE_MIN_SDK - 1,
        mutableFlag = 0b010,
        immutableFlag = 0b100,
      ),
    )
  }

  @Test
  fun usesMutableFlagWhenMutableIsRequestedAndAvailable() {
    assertEquals(
      0b011,
      tabbyPendingIntentFlagsFromPlatformState(
        flags = 0b001,
        mutable = true,
        platformSdk = TABBY_PENDING_INTENT_MUTABLE_MIN_SDK,
        mutableFlag = 0b010,
        immutableFlag = 0b100,
      ),
    )
  }
}
