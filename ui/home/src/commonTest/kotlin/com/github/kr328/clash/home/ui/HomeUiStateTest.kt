package com.github.kr328.clash.home.ui

import androidx.compose.material3.SnackbarResult
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.core.model.Traffic
import com.github.kr328.clash.core.model.TunnelState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class HomeUiStateTest {
  @Test
  fun createsInitialHomeUiState() {
    assertEquals(HomeUiState(), homeInitialUiState())
  }

  @Test
  fun createsInitialHomeEventState() {
    val event: HomeEventState<String> = homeInitialEventState()

    assertEquals(HomeEventState.Idle, event)
  }

  @Test
  fun homeStartActionStartsEngineForImportedActiveProfile() {
    assertEquals(
      HomeStartAction.StartEngine,
      homeStartAction(profile(imported = true)),
    )
  }

  @Test
  fun homeStartActionShowsNoProfileMessageWhenActiveProfileIsMissingOrNotImported() {
    assertEquals(
      HomeStartAction.ShowNoProfileMessage,
      homeStartAction(null),
    )
    assertEquals(
      HomeStartAction.ShowNoProfileMessage,
      homeStartAction(profile(imported = false)),
    )
  }

  @Test
  fun homeStartEventStateShowsNoProfileMessageOnlyWhenProfileIsMissing() {
    assertEquals(null, homeStartEventState(HomeStartAction.StartEngine))
    assertEquals(
      HomeEventState.ShowNoProfileMessage,
      homeStartEventState(HomeStartAction.ShowNoProfileMessage),
    )
  }

  @Test
  fun homeEventStateCarriesVpnPermissionPayloadAsGenericValue() {
    val permissionRequest = "vpn-permission-intent"
    val event: HomeEventState<String> = homeVpnPermissionEventState(permissionRequest)

    assertEquals(HomeEventState.RequestVpnPermission(permissionRequest), event)
  }

  @Test
  fun homeEventPlatformActionMapsEventStates() {
    assertEquals(
      HomeEventPlatformAction.Ignore,
      homeEventPlatformAction(HomeEventState.Idle),
    )
    assertEquals(
      HomeEventPlatformAction.RequestVpnPermission("vpn-permission-intent"),
      homeEventPlatformAction(HomeEventState.RequestVpnPermission("vpn-permission-intent")),
    )
    assertEquals(
      HomeEventPlatformAction.ShowNoProfileMessage,
      homeEventPlatformAction(HomeEventState.ShowNoProfileMessage),
    )
    assertEquals(
      HomeEventPlatformAction.ShowMessage("Stopped by system"),
      homeEventPlatformAction(HomeEventState.ShowMessage("Stopped by system")),
    )
  }

  @Test
  fun homeBroadcastEventStateShowsStoppedMessageOnlyWhenPresent() {
    assertEquals(
      HomeEventState.ShowMessage("Stopped by system"),
      homeBroadcastEventState(
        HomeBroadcastAction(shouldFetch = true, stoppedMessage = "Stopped by system")
      ),
    )
    assertEquals(
      null,
      homeBroadcastEventState(HomeBroadcastAction(shouldFetch = true)),
    )
  }

  @Test
  fun homeStartFailureEventStateCarriesLocalizedMessage() {
    assertEquals(
      HomeEventState.ShowMessage("Unable to start VPN"),
      homeStartFailureEventState("Unable to start VPN"),
    )
  }

  @Test
  fun homeEngineStartResultsMapToEvents() {
    assertEquals(null, homeEngineStartEventState(homeEngineStartedResult()))
    assertEquals(
      HomeEventState.RequestVpnPermission("vpn-permission-token"),
      homeEngineStartEventState(homeEngineVpnPermissionResult("vpn-permission-token")),
    )
    assertEquals(
      HomeEventState.ShowMessage("Unable to start VPN"),
      homeEngineStartEventState(homeEngineStartFailedResult("Unable to start VPN")),
    )
  }

  @Test
  fun consumedEventStateResetsToIdle() {
    val event: HomeEventState<String> = homeConsumedEventState()

    assertEquals(HomeEventState.Idle, event)
  }

  @Test
  fun homeBroadcastActionFetchesForStateChangingEvents() {
    listOf(
        HomeBroadcastEventKind.ServiceRecreated,
        HomeBroadcastEventKind.Started,
        HomeBroadcastEventKind.ProfileChanged,
        HomeBroadcastEventKind.ProfileLoaded,
      )
      .forEach { kind ->
        assertEquals(
          HomeBroadcastAction(shouldFetch = true),
          homeBroadcastAction(kind),
        )
      }
  }

  @Test
  fun homeBroadcastActionFetchesStoppedEventAndPreservesOptionalMessage() {
    assertEquals(
      HomeBroadcastAction(shouldFetch = true, stoppedMessage = "Stopped by system"),
      homeBroadcastAction(
        HomeBroadcastEventKind.Stopped,
        stoppedMessage = "Stopped by system",
      ),
    )
    assertEquals(
      HomeBroadcastAction(shouldFetch = true),
      homeBroadcastAction(HomeBroadcastEventKind.Stopped),
    )
  }

  @Test
  fun homeBroadcastActionAcceptsPlatformPayloadEvent() {
    assertEquals(
      HomeBroadcastAction(shouldFetch = true, stoppedMessage = "Stopped by system"),
      homeBroadcastAction(
        HomeBroadcastEvent(
          kind = HomeBroadcastEventKind.Stopped,
          stoppedMessage = "Stopped by system",
        )
      ),
    )
    assertEquals(
      HomeBroadcastAction(shouldFetch = false),
      homeBroadcastAction(HomeBroadcastEvent(HomeBroadcastEventKind.ProfileUpdateCompleted)),
    )
  }

  @Test
  fun homeBroadcastActionIgnoresProfileUpdateEvents() {
    assertEquals(
      HomeBroadcastAction(shouldFetch = false),
      homeBroadcastAction(HomeBroadcastEventKind.ProfileUpdateCompleted),
    )
    assertEquals(
      HomeBroadcastAction(shouldFetch = false),
      homeBroadcastAction(HomeBroadcastEventKind.ProfileUpdateFailed),
    )
  }

  @Test
  fun homeToggleActionStopsRunningClashAndStartsStoppedClash() {
    assertEquals(
      HomeToggleAction.StopClash,
      homeToggleAction(clashRunning = true),
    )
    assertEquals(
      HomeToggleAction.StartClash,
      homeToggleAction(clashRunning = false),
    )
  }

  @Test
  fun homeActiveFetchActionRequestsFetchOnlyWhenActive() {
    assertEquals(
      HomeActiveFetchAction.RequestFetch,
      homeActiveFetchAction(active = true),
    )
    assertEquals(
      HomeActiveFetchAction.Ignore,
      homeActiveFetchAction(active = false),
    )
  }

  @Test
  fun homeStartedStateFromStartStopEventStartsStopsAndKeepsExistingState() {
    assertEquals(
      true,
      homeStartedStateFromStartStopEvent(
        currentStarted = false,
        isStartEvent = true,
        isStopEvent = false,
      ),
    )
    assertEquals(
      false,
      homeStartedStateFromStartStopEvent(
        currentStarted = true,
        isStartEvent = false,
        isStopEvent = true,
      ),
    )
    assertEquals(
      true,
      homeStartedStateFromStartStopEvent(
        currentStarted = true,
        isStartEvent = false,
        isStopEvent = false,
      ),
    )
    assertEquals(
      false,
      homeStartedStateFromStartStopEvent(
        currentStarted = false,
        isStartEvent = false,
        isStopEvent = false,
      ),
    )
  }

  @Test
  fun homeTrafficPollActionQueriesOnlyWhenClashIsRunning() {
    assertEquals(
      HomeTrafficPollAction.QueryTraffic,
      homeTrafficPollAction(clashRunning = true),
    )
    assertEquals(
      HomeTrafficPollAction.Ignore,
      homeTrafficPollAction(clashRunning = false),
    )
  }

  @Test
  fun homeTrafficPollActionRequiresActiveScreenAndRunningClash() {
    assertEquals(
      HomeTrafficPollAction.QueryTraffic,
      homeTrafficPollAction(started = true, clashRunning = true),
    )
    assertEquals(
      HomeTrafficPollAction.Ignore,
      homeTrafficPollAction(started = false, clashRunning = true),
    )
    assertEquals(
      HomeTrafficPollAction.Ignore,
      homeTrafficPollAction(started = true, clashRunning = false),
    )
  }

  @Test
  fun homeTrafficTotalTextFormatsByteTotal() {
    assertEquals("0 B", homeTrafficTotalText(Traffic.fromBytes(upload = 0, download = 0)))
    assertEquals("460 B", homeTrafficTotalText(Traffic.fromBytes(upload = 120, download = 340)))
  }

  @Test
  fun homeTrafficTotalTextFormatsBinaryUnits() {
    assertEquals(
      "2 KiB",
      homeTrafficTotalText(Traffic.fromBytes(upload = 1536, download = 512)),
    )
    assertEquals(
      "5.5 MiB",
      homeTrafficTotalText(Traffic.fromBytes(upload = 5 * 1024 * 1024, download = 512 * 1024)),
    )
  }

  @Test
  fun homeNoProfileSnackbarActionOpensProfilesOnlyWhenActionIsPerformed() {
    assertEquals(
      HomeNoProfileSnackbarAction.OpenProfiles,
      homeNoProfileSnackbarAction(SnackbarActionResult.ActionPerformed),
    )
    assertEquals(
      HomeNoProfileSnackbarAction.Ignore,
      homeNoProfileSnackbarAction(SnackbarActionResult.Dismissed),
    )
  }

  @Test
  fun snackbarActionResultFromPlatformActionPerformedMapsBooleanResult() {
    assertEquals(
      SnackbarActionResult.ActionPerformed,
      snackbarActionResultFromPlatformActionPerformed(actionPerformed = true),
    )
    assertEquals(
      SnackbarActionResult.Dismissed,
      snackbarActionResultFromPlatformActionPerformed(actionPerformed = false),
    )
  }

  @Test
  fun snackbarResultMapsToSnackbarActionResult() {
    assertEquals(
      SnackbarActionResult.ActionPerformed,
      SnackbarResult.ActionPerformed.toSnackbarActionResult(),
    )
    assertEquals(
      SnackbarActionResult.Dismissed,
      SnackbarResult.Dismissed.toSnackbarActionResult(),
    )
  }

  @Test
  fun homeVpnPermissionResultMapsGrantedBoolean() {
    assertEquals(
      HomeVpnPermissionResult.Granted,
      homeVpnPermissionResult(granted = true),
    )
    assertEquals(
      HomeVpnPermissionResult.Denied,
      homeVpnPermissionResult(granted = false),
    )
  }

  @Test
  fun homeVpnPermissionResultActionStartsEngineOnlyWhenPermissionIsGranted() {
    assertEquals(
      HomeVpnPermissionResultAction.StartEngine,
      homeVpnPermissionResultAction(HomeVpnPermissionResult.Granted),
    )
    assertEquals(
      HomeVpnPermissionResultAction.Ignore,
      homeVpnPermissionResultAction(HomeVpnPermissionResult.Denied),
    )
  }

  @Test
  fun homeVpnPermissionResultActionFromGrantedStartsEngineOnlyForGrantedPermission() {
    assertEquals(
      HomeVpnPermissionResultAction.StartEngine,
      homeVpnPermissionResultActionFromGranted(granted = true),
    )
    assertEquals(
      HomeVpnPermissionResultAction.Ignore,
      homeVpnPermissionResultActionFromGranted(granted = false),
    )
  }

  @Test
  fun homeModeLabelMapsTunnelStateModes() {
    assertEquals(HomeModeLabel.Direct, homeModeLabel(TunnelState.Mode.Direct))
    assertEquals(HomeModeLabel.Global, homeModeLabel(TunnelState.Mode.Global))
    assertEquals(HomeModeLabel.Rule, homeModeLabel(TunnelState.Mode.Rule))
  }

  @Test
  fun homeModeLabelPlatformTokenMapsModeLabels() {
    assertEquals(
      "direct",
      homeModeLabelPlatformToken(
        label = HomeModeLabel.Direct,
        directMode = "direct",
        globalMode = "global",
        ruleMode = "rule",
      ),
    )
    assertEquals(
      "global",
      homeModeLabelPlatformToken(
        label = HomeModeLabel.Global,
        directMode = "direct",
        globalMode = "global",
        ruleMode = "rule",
      ),
    )
    assertEquals(
      "rule",
      homeModeLabelPlatformToken(
        label = HomeModeLabel.Rule,
        directMode = "direct",
        globalMode = "global",
        ruleMode = "rule",
      ),
    )
  }

  @Test
  fun fetchedStateShowsModeWhenClashIsRunningAndKeepsForwardedTraffic() {
    val state =
      HomeUiState(forwarded = "10 MB")
        .withFetchedHomeState(
          clashRunning = true,
          mode = "Rule",
          hasProviders = true,
          profileName = "Daily",
        )

    assertEquals("10 MB", state.forwarded)
    assertEquals("Rule", state.mode)
    assertEquals(true, state.hasProviders)
    assertEquals("Daily", state.profileName)
  }

  @Test
  fun fetchedStateClearsModeWhenClashIsStoppedAndUpdatesProfileAndProviders() {
    val state =
      HomeUiState(forwarded = "10 MB", mode = "Global", hasProviders = true, profileName = "Old")
        .withFetchedHomeState(
          clashRunning = false,
          mode = "Rule",
          hasProviders = true,
          profileName = "New",
        )

    assertEquals("10 MB", state.forwarded)
    assertEquals(null, state.mode)
    assertEquals(false, state.hasProviders)
    assertEquals("New", state.profileName)
  }

  @Test
  fun forwardedTrafficUpdatePreservesFetchedStateFields() {
    val state =
      HomeUiState(mode = "Direct", hasProviders = true, profileName = "Daily")
        .withForwardedTraffic("42 MB")

    assertEquals("42 MB", state.forwarded)
    assertEquals("Direct", state.mode)
    assertEquals(true, state.hasProviders)
    assertEquals("Daily", state.profileName)
  }

  private fun profile(imported: Boolean): Profile {
    return Profile(
      uuid = Uuid.parse("00000000-0000-0000-0000-000000000001"),
      name = "Profile",
      type = Profile.Type.Url,
      source = "https://example.com/config.yaml",
      active = true,
      interval = 0,
      upload = 0,
      download = 0,
      total = 0,
      expire = 0,
      updatedAt = 0,
      imported = imported,
      pending = !imported,
    )
  }
}
