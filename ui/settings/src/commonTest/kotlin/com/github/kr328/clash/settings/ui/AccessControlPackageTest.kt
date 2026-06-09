package com.github.kr328.clash.settings.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessControlPackageTest {
  @Test
  fun mapsPlatformPayloadToAccessControlPackage() {
    assertEquals(
      AccessControlPackage(
        packageName = "com.example.alpha",
        label = "Alpha Tool",
        installTime = 42,
        updateDate = 84,
      ),
      accessControlPackageFromPlatformPayload(
        app =
          TestPlatformApp(
            packageName = "com.example.alpha",
            label = "Alpha Tool",
            firstInstallTime = 42,
            lastUpdateTime = 84,
          ),
        packageName = TestPlatformApp::packageName,
        label = TestPlatformApp::label,
        installTime = TestPlatformApp::firstInstallTime,
        updateDate = TestPlatformApp::lastUpdateTime,
      ),
    )
  }

  private data class TestPlatformApp(
    val packageName: String,
    val label: String,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
  )
}
