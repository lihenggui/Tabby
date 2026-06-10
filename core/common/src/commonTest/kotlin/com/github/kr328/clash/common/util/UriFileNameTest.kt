package com.github.kr328.clash.common.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UriFileNameTest {
  @Test
  fun returnsLastPathSegmentFromSchemeSpecificPart() {
    assertEquals(
      "profile.yaml",
      tabbyFileNameFromSchemeSpecificPart("//provider/imports/profile.yaml"),
    )
  }

  @Test
  fun returnsWholePayloadWhenNoPathSeparatorExists() {
    assertEquals("profile.yaml", tabbyFileNameFromSchemeSpecificPart("profile.yaml"))
  }

  @Test
  fun returnsEmptyNameFromEmptyPayload() {
    assertEquals("", tabbyFileNameFromSchemeSpecificPart(""))
  }

  @Test
  fun ignoresMissingPayload() {
    assertNull(tabbyFileNameFromSchemeSpecificPart(null))
  }
}
