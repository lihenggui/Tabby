package com.github.kr328.clash.home.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class HelpContentStateTest {
  @Test
  fun createsInitialHelpContentState() {
    assertEquals(HelpContentState(), helpInitialContentState())
  }

  @Test
  fun updateCheckStateChangesPreserveVersionInfo() {
    val initial = HelpContentState(appVersion = "1.0.0", coreVersion = "Meta 1.0")
    val started = initial.withUpdateCheckStarted()
    val finished = started.withUpdateCheckFinished()

    assertEquals(true, started.checkingForUpdates)
    assertEquals("1.0.0", started.appVersion)
    assertEquals("Meta 1.0", started.coreVersion)
    assertEquals(false, finished.checkingForUpdates)
    assertEquals("1.0.0", finished.appVersion)
    assertEquals("Meta 1.0", finished.coreVersion)
  }

  @Test
  fun versionInfoUpdatePreservesUpdateCheckState() {
    val state =
      HelpContentState(checkingForUpdates = true)
        .withVersionInfo(appVersion = "2.0.0 - abc123", coreVersion = "Meta 2.0")

    assertEquals(true, state.checkingForUpdates)
    assertEquals("2.0.0 - abc123", state.appVersion)
    assertEquals("Meta 2.0", state.coreVersion)
  }

  @Test
  fun platformVersionPayloadMapsToVersionInfo() {
    val versionInfo =
      helpVersionInfoFromPlatformPayload(
        versionName = "2.0.0",
        buildCommit = "abc123",
        coreVersion = "Meta 2.0",
      )
    val state = HelpContentState(checkingForUpdates = true).withVersionInfo(versionInfo)

    assertEquals(
      HelpVersionInfo(appVersion = "2.0.0 - abc123", coreVersion = "Meta 2.0"),
      versionInfo,
    )
    assertEquals(true, state.checkingForUpdates)
    assertEquals("2.0.0 - abc123", state.appVersion)
    assertEquals("Meta 2.0", state.coreVersion)
  }

  @Test
  fun appVersionInfoIncludesBuildCommit() {
    assertEquals(
      "1.2.3 - abc123",
      formatAppVersionInfo(versionName = "1.2.3", buildCommit = "abc123"),
    )
  }

  @Test
  fun appVersionInfoPreservesNullVersionNameDisplay() {
    assertEquals(
      "null - abc123",
      formatAppVersionInfo(versionName = null, buildCommit = "abc123"),
    )
  }
}
