package com.github.kr328.clash.common.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PackageNameUriPayloadTest {
  @Test
  fun parsesPackageNamePayloadWhenSchemeMatches() {
    assertEquals(
      "io.github.example",
      tabbyPackageNameFromUriPayload(
        scheme = TABBY_PACKAGE_NAME_URI_SCHEME,
        schemeSpecificPart = "io.github.example",
      ),
    )
  }

  @Test
  fun ignoresPayloadWhenSchemeDoesNotMatch() {
    assertNull(
      tabbyPackageNameFromUriPayload(
        scheme = "application",
        schemeSpecificPart = "io.github.example",
      )
    )
  }

  @Test
  fun ignoresMissingPackageNamePayload() {
    assertNull(
      tabbyPackageNameFromUriPayload(
        scheme = TABBY_PACKAGE_NAME_URI_SCHEME,
        schemeSpecificPart = null,
      )
    )
  }

  @Test
  fun matchesPackageSchemeCaseSensitively() {
    assertNull(
      tabbyPackageNameFromUriPayload(
        scheme = "PACKAGE",
        schemeSpecificPart = "io.github.example",
      )
    )
  }
}
