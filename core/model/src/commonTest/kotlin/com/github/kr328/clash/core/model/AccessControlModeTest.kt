package com.github.kr328.clash.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessControlModeTest {
  @Test
  fun accessControlPackagePlanDoesNothingWhenAllAppsAreAccepted() {
    assertEquals(
      AccessControlPackagePlan(),
      accessControlPackagePlan(
        mode = AccessControlMode.AcceptAll,
        selectedPackages = setOf("com.example.browser"),
        ownPackageName = "com.github.kr328.clash",
      ),
    )
  }

  @Test
  fun accessControlPackagePlanAllowsSelectedPackagesAndOwnPackage() {
    assertEquals(
      AccessControlPackagePlan(
        allowedPackages = setOf("com.example.browser", "com.github.kr328.clash")
      ),
      accessControlPackagePlan(
        mode = AccessControlMode.AcceptSelected,
        selectedPackages = setOf("com.example.browser"),
        ownPackageName = "com.github.kr328.clash",
      ),
    )
  }

  @Test
  fun accessControlPackagePlanDoesNotDuplicateOwnPackageWhenAllowed() {
    assertEquals(
      AccessControlPackagePlan(
        allowedPackages = setOf("com.github.kr328.clash", "com.example.browser")
      ),
      accessControlPackagePlan(
        mode = AccessControlMode.AcceptSelected,
        selectedPackages = setOf("com.github.kr328.clash", "com.example.browser"),
        ownPackageName = "com.github.kr328.clash",
      ),
    )
  }

  @Test
  fun accessControlPackagePlanDeniesSelectedPackagesExceptOwnPackage() {
    assertEquals(
      AccessControlPackagePlan(disallowedPackages = setOf("com.example.browser")),
      accessControlPackagePlan(
        mode = AccessControlMode.DenySelected,
        selectedPackages = setOf("com.github.kr328.clash", "com.example.browser"),
        ownPackageName = "com.github.kr328.clash",
      ),
    )
  }
}
