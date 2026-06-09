package com.github.kr328.clash.common.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class IntentUuidPayloadTest {
  @Test
  fun parsesUuidPayloadWhenSchemeMatches() {
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")

    assertEquals(
      uuid,
      tabbyUuidFromUriPayload(
        scheme = TABBY_UUID_URI_SCHEME,
        schemeSpecificPart = uuid.toString(),
      ),
    )
  }

  @Test
  fun ignoresPayloadWhenSchemeDoesNotMatch() {
    assertNull(
      tabbyUuidFromUriPayload(
        scheme = "profile",
        schemeSpecificPart = "00000000-0000-0000-0000-000000000001",
      )
    )
  }

  @Test
  fun ignoresMissingUuidPayload() {
    assertNull(
      tabbyUuidFromUriPayload(
        scheme = TABBY_UUID_URI_SCHEME,
        schemeSpecificPart = null,
      )
    )
  }

  @Test
  fun rejectsInvalidUuidPayloadWhenSchemeMatches() {
    assertFailsWith<IllegalArgumentException> {
      tabbyUuidFromUriPayload(
        scheme = TABBY_UUID_URI_SCHEME,
        schemeSpecificPart = "not-a-uuid",
      )
    }
  }

  @Test
  fun formatsUuidPayloadForPlatformUri() {
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")

    assertEquals(uuid.toString(), tabbyUuidUriSchemeSpecificPart(uuid))
  }
}
