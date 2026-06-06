package com.github.kr328.clash.home.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HelpUpdateCheckActionTest {
  @Test
  fun updateCheckRequestStartsWhenNotAlreadyChecking() {
    assertEquals(
      HelpUpdateCheckRequestAction.StartCheck,
      helpUpdateCheckRequestAction(HelpContentState(checkingForUpdates = false)),
    )
  }

  @Test
  fun updateCheckRequestIgnoresWhenAlreadyChecking() {
    assertEquals(
      HelpUpdateCheckRequestAction.Ignore,
      helpUpdateCheckRequestAction(HelpContentState(checkingForUpdates = true)),
    )
  }

  @Test
  fun updateCheckActionShowsFailureWhenLatestTagIsMissing() {
    assertEquals(
      HelpUpdateCheckAction.ShowUpdateCheckFailedMessage,
      helpUpdateCheckAction(latestTag = null, localVersion = "1.0.0"),
    )
  }

  @Test
  fun updateCheckActionShowsUpdateAvailableWhenLatestVersionIsNewer() {
    assertEquals(
      HelpUpdateCheckAction.ShowUpdateAvailable,
      helpUpdateCheckAction(latestTag = "1.2.0", localVersion = "1.1.9"),
    )
  }

  @Test
  fun updateCheckActionShowsAlreadyUpToDateWhenLocalVersionMatchesLatest() {
    assertEquals(
      HelpUpdateCheckAction.ShowAlreadyUpToDateMessage,
      helpUpdateCheckAction(latestTag = "1.2.0", localVersion = "1.2.0"),
    )
  }

  @Test
  fun updateCheckActionShowsAlreadyUpToDateWhenLocalVersionIsNewer() {
    assertEquals(
      HelpUpdateCheckAction.ShowAlreadyUpToDateMessage,
      helpUpdateCheckAction(latestTag = "1.2.0", localVersion = "1.3.0"),
    )
  }

  @Test
  fun updateCheckActionKeepsInvalidVersionHandlingWithCaller() {
    assertFailsWith<IllegalArgumentException> {
      helpUpdateCheckAction(latestTag = "not-a-version", localVersion = "1.0.0")
    }
  }
}
