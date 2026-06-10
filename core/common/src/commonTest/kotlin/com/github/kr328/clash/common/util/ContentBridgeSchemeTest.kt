package com.github.kr328.clash.common.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ContentBridgeSchemeTest {
  @Test
  fun acceptsContentScheme() {
    assertTrue(tabbyContentBridgeSupportsScheme("content"))
  }

  @Test
  fun rejectsNonContentSchemes() {
    assertFalse(tabbyContentBridgeSupportsScheme(null))
    assertFalse(tabbyContentBridgeSupportsScheme(""))
    assertFalse(tabbyContentBridgeSupportsScheme("http"))
    assertFalse(tabbyContentBridgeSupportsScheme("file"))
  }

  @Test
  fun matchesContentSchemeCaseSensitively() {
    assertFalse(tabbyContentBridgeSupportsScheme("CONTENT"))
  }
}
