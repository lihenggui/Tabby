package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileQrSourceTest {
  @Test
  fun prefersRawValueWhenPresent() {
    assertEquals(
      "https://example.com/raw-value.yaml",
      decodeProfileQrSource(
        rawValue = "https://example.com/raw-value.yaml",
        rawBytes = "https://example.com/raw-bytes.yaml".encodeToByteArray(),
      ),
    )
  }

  @Test
  fun decodesRawBytesWhenRawValueIsMissing() {
    assertEquals(
      "https://example.com/raw-bytes.yaml",
      decodeProfileQrSource(
        rawValue = null,
        rawBytes = "https://example.com/raw-bytes.yaml".encodeToByteArray(),
      ),
    )
  }

  @Test
  fun returnsEmptySourceWhenPayloadIsMissing() {
    assertEquals("", decodeProfileQrSource(rawValue = null, rawBytes = null))
  }
}
