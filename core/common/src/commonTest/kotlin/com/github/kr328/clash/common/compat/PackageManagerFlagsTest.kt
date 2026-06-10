package com.github.kr328.clash.common.compat

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PackageManagerFlagsTest {
  @Test
  fun usesLegacyFlagsBeforeTypedPackageManagerFlagsAreAvailable() {
    assertFalse(
      tabbyPackageManagerUsesTypedFlags(platformSdk = TABBY_PACKAGE_MANAGER_TYPED_FLAGS_MIN_SDK - 1)
    )
  }

  @Test
  fun usesTypedFlagsWhenAvailable() {
    assertTrue(
      tabbyPackageManagerUsesTypedFlags(platformSdk = TABBY_PACKAGE_MANAGER_TYPED_FLAGS_MIN_SDK)
    )
  }
}
