package com.github.kr328.clash.home.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class HelpUpdateCheckActionTest {
  @Test
  fun createsInitialHelpEventState() {
    assertEquals(HelpEventState.Idle, helpInitialEventState())
  }

  @Test
  fun helpEventRouteEffectMapsEventStates() {
    assertEquals(
      HelpEventRouteEffect.Ignore,
      helpEventRouteEffect(HelpEventState.Idle),
    )
    assertEquals(
      HelpEventRouteEffect.ShowMessage("already up to date"),
      helpEventRouteEffect(HelpEventState.ShowMessage("already up to date")),
    )
    assertEquals(
      HelpEventRouteEffect.ShowUpdateAvailable("https://example.com/releases"),
      helpEventRouteEffect(HelpEventState.UpdateAvailable("https://example.com/releases")),
    )
  }

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
  fun localVersionLoadIgnoresWhenLatestTagIsMissing() {
    assertEquals(
      HelpLocalVersionLoadAction.Ignore,
      helpLocalVersionLoadAction(latestTag = null),
    )
  }

  @Test
  fun localVersionLoadStartsWhenLatestTagExists() {
    assertEquals(
      HelpLocalVersionLoadAction.Load,
      helpLocalVersionLoadAction(latestTag = "1.2.0"),
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
  fun updateCheckActionShowsFailureWhenLocalVersionIsMissing() {
    assertEquals(
      HelpUpdateCheckAction.ShowUpdateCheckFailedMessage,
      helpUpdateCheckAction(latestTag = "1.2.0", localVersion = null),
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
  fun updateCheckActionShowsFailureWhenLatestVersionIsInvalid() {
    assertEquals(
      HelpUpdateCheckAction.ShowUpdateCheckFailedMessage,
      helpUpdateCheckAction(latestTag = "not-a-version", localVersion = "1.0.0"),
    )
  }

  @Test
  fun updateCheckActionShowsFailureWhenLocalVersionIsInvalid() {
    assertEquals(
      HelpUpdateCheckAction.ShowUpdateCheckFailedMessage,
      helpUpdateCheckAction(latestTag = "1.2.0", localVersion = "not-a-version"),
    )
  }

  @Test
  fun updateCheckEventStateMapsActionToUpdateAvailableEvent() {
    assertEquals(
      HelpEventState.UpdateAvailable("https://example.com/releases"),
      helpUpdateCheckEventState(
        action = HelpUpdateCheckAction.ShowUpdateAvailable,
        releasesUrl = "https://example.com/releases",
        alreadyUpToDateMessage = "already up to date",
        updateCheckFailedMessage = "failed",
      ),
    )
  }

  @Test
  fun updateCheckEventStateMapsActionToMessageEvent() {
    assertEquals(
      HelpEventState.ShowMessage("already up to date"),
      helpUpdateCheckEventState(
        action = HelpUpdateCheckAction.ShowAlreadyUpToDateMessage,
        releasesUrl = "https://example.com/releases",
        alreadyUpToDateMessage = "already up to date",
        updateCheckFailedMessage = "failed",
      ),
    )
    assertEquals(
      HelpEventState.ShowMessage("failed"),
      helpUpdateCheckEventState(
        action = HelpUpdateCheckAction.ShowUpdateCheckFailedMessage,
        releasesUrl = "https://example.com/releases",
        alreadyUpToDateMessage = "already up to date",
        updateCheckFailedMessage = "failed",
      ),
    )
  }

  @Test
  fun updateCheckFailureEventStateMapsToFailureMessage() {
    assertEquals(
      HelpEventState.ShowMessage("failed"),
      helpUpdateCheckFailureEventState(updateCheckFailedMessage = "failed"),
    )
  }

  @Test
  fun updateAvailableSnackbarActionOpensReleasesOnlyWhenActionIsPerformed() {
    assertEquals(
      HelpUpdateAvailableSnackbarAction.OpenReleases,
      helpUpdateAvailableSnackbarAction(SnackbarActionResult.ActionPerformed),
    )
    assertEquals(
      HelpUpdateAvailableSnackbarAction.Ignore,
      helpUpdateAvailableSnackbarAction(SnackbarActionResult.Dismissed),
    )
  }

  @Test
  fun consumedEventStateResetsToIdle() {
    assertEquals(HelpEventState.Idle, helpConsumedEventState())
  }
}
