package com.github.kr328.clash.common.service

import kotlin.test.Test
import kotlin.test.assertEquals

class ServicePreferenceBackendTest {
  @Test
  fun usesDirectBackendForLocalServiceContext() {
    assertEquals(
      TabbyServicePreferenceBackend.Direct,
      tabbyServicePreferenceBackend(localServiceContext = true),
    )
  }

  @Test
  fun usesMultiProcessBackendForNonServiceContext() {
    assertEquals(
      TabbyServicePreferenceBackend.MultiProcess,
      tabbyServicePreferenceBackend(localServiceContext = false),
    )
  }
}
