package com.github.kr328.clash.common.compat

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SerializableExtraFlagsTest {
  @Test
  fun usesLegacySerializableExtraBeforeTypedApiIsAvailable() {
    assertFalse(
      tabbySerializableExtraUsesTypedApi(
        platformSdk = TABBY_SERIALIZABLE_EXTRA_TYPED_API_MIN_SDK - 1
      )
    )
  }

  @Test
  fun usesTypedSerializableExtraWhenAvailable() {
    assertTrue(
      tabbySerializableExtraUsesTypedApi(platformSdk = TABBY_SERIALIZABLE_EXTRA_TYPED_API_MIN_SDK)
    )
  }
}
