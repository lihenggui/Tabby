package com.github.kr328.clash.home.ui

import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.core.model.TunnelState

internal data class HomeUiState(
  val forwarded: String? = null,
  val mode: String? = null,
  val profileName: String? = null,
  val hasProviders: Boolean = false,
)

internal data class HomeFetchedPlatformState(
  val clashRunning: Boolean,
  val mode: String,
  val hasProviders: Boolean,
  val profileName: String?,
)

internal fun homeInitialUiState(): HomeUiState {
  return HomeUiState()
}

internal enum class HomeStartAction {
  StartEngine,
  ShowNoProfileMessage,
}

internal enum class HomeBroadcastEventKind {
  ServiceRecreated,
  Started,
  Stopped,
  ProfileChanged,
  ProfileUpdateCompleted,
  ProfileUpdateFailed,
  ProfileLoaded,
}

internal enum class HomePlatformBroadcastEventKind {
  ServiceRecreated,
  Started,
  Stopped,
  ProfileChanged,
  ProfileUpdateCompleted,
  ProfileUpdateFailed,
  ProfileLoaded,
}

internal data class HomeBroadcastEvent(
  val kind: HomeBroadcastEventKind,
  val stoppedMessage: String? = null,
)

internal data class HomeBroadcastAction(
  val shouldFetch: Boolean,
  val stoppedMessage: String? = null,
)

internal enum class HomeToggleAction {
  StartClash,
  StopClash,
}

internal enum class HomeActiveFetchAction {
  RequestFetch,
  Ignore,
}

internal enum class HomeTrafficPollAction {
  QueryTraffic,
  Ignore,
}

internal enum class HomeNoProfileSnackbarAction {
  OpenProfiles,
  Ignore,
}

internal enum class HomeVpnPermissionResult {
  Granted,
  Denied,
}

internal enum class HomeVpnPermissionResultAction {
  StartEngine,
  Ignore,
}

internal enum class HomeModeLabel {
  Direct,
  Global,
  Rule,
}

internal sealed interface HomeEventState<out VpnPermissionT> {
  data object Idle : HomeEventState<Nothing>

  data class RequestVpnPermission<out VpnPermissionT>(val permissionRequest: VpnPermissionT) :
    HomeEventState<VpnPermissionT>

  data object ShowNoProfileMessage : HomeEventState<Nothing>

  data class ShowMessage(val message: String) : HomeEventState<Nothing>
}

internal sealed interface HomeEventPlatformAction<out VpnPermissionT> {
  data object Ignore : HomeEventPlatformAction<Nothing>

  data class RequestVpnPermission<out VpnPermissionT>(val permissionRequest: VpnPermissionT) :
    HomeEventPlatformAction<VpnPermissionT>

  data object ShowNoProfileMessage : HomeEventPlatformAction<Nothing>

  data class ShowMessage(val message: String) : HomeEventPlatformAction<Nothing>
}

internal sealed interface HomeEngineStartResult<out VpnPermissionT> {
  data object Started : HomeEngineStartResult<Nothing>

  data class VpnPermissionRequired<out VpnPermissionT>(val permissionRequest: VpnPermissionT) :
    HomeEngineStartResult<VpnPermissionT>

  data class Failed(val message: String) : HomeEngineStartResult<Nothing>
}

internal fun homeInitialEventState(): HomeEventState<Nothing> {
  return HomeEventState.Idle
}

internal fun <VpnPermissionT> homeEventPlatformAction(
  eventState: HomeEventState<VpnPermissionT>
): HomeEventPlatformAction<VpnPermissionT> {
  return when (eventState) {
    HomeEventState.Idle -> HomeEventPlatformAction.Ignore
    is HomeEventState.RequestVpnPermission ->
      HomeEventPlatformAction.RequestVpnPermission(eventState.permissionRequest)
    HomeEventState.ShowNoProfileMessage -> HomeEventPlatformAction.ShowNoProfileMessage
    is HomeEventState.ShowMessage -> HomeEventPlatformAction.ShowMessage(eventState.message)
  }
}

internal fun homeStartAction(activeProfile: Profile?): HomeStartAction {
  return if (activeProfile?.imported == true) HomeStartAction.StartEngine
  else HomeStartAction.ShowNoProfileMessage
}

internal fun homeStartEventState(action: HomeStartAction): HomeEventState<Nothing>? {
  return when (action) {
    HomeStartAction.StartEngine -> null
    HomeStartAction.ShowNoProfileMessage -> HomeEventState.ShowNoProfileMessage
  }
}

internal fun homeBroadcastEventState(action: HomeBroadcastAction): HomeEventState<Nothing>? {
  return action.stoppedMessage?.let { HomeEventState.ShowMessage(it) }
}

internal fun homeBroadcastEventFromPlatformPayload(
  kind: HomePlatformBroadcastEventKind,
  stoppedMessage: String? = null,
): HomeBroadcastEvent {
  val eventKind =
    when (kind) {
      HomePlatformBroadcastEventKind.ServiceRecreated -> HomeBroadcastEventKind.ServiceRecreated
      HomePlatformBroadcastEventKind.Started -> HomeBroadcastEventKind.Started
      HomePlatformBroadcastEventKind.Stopped -> HomeBroadcastEventKind.Stopped
      HomePlatformBroadcastEventKind.ProfileChanged -> HomeBroadcastEventKind.ProfileChanged
      HomePlatformBroadcastEventKind.ProfileUpdateCompleted ->
        HomeBroadcastEventKind.ProfileUpdateCompleted
      HomePlatformBroadcastEventKind.ProfileUpdateFailed ->
        HomeBroadcastEventKind.ProfileUpdateFailed
      HomePlatformBroadcastEventKind.ProfileLoaded -> HomeBroadcastEventKind.ProfileLoaded
    }

  return HomeBroadcastEvent(
    kind = eventKind,
    stoppedMessage =
      when (eventKind) {
        HomeBroadcastEventKind.Stopped -> stoppedMessage
        HomeBroadcastEventKind.ServiceRecreated,
        HomeBroadcastEventKind.Started,
        HomeBroadcastEventKind.ProfileChanged,
        HomeBroadcastEventKind.ProfileUpdateCompleted,
        HomeBroadcastEventKind.ProfileUpdateFailed,
        HomeBroadcastEventKind.ProfileLoaded -> null
      },
  )
}

internal fun homeBroadcastAction(event: HomeBroadcastEvent): HomeBroadcastAction {
  return homeBroadcastAction(
    kind = event.kind,
    stoppedMessage = event.stoppedMessage,
  )
}

internal fun <VpnPermissionT> homeVpnPermissionEventState(
  permissionRequest: VpnPermissionT
): HomeEventState<VpnPermissionT> {
  return HomeEventState.RequestVpnPermission(permissionRequest)
}

internal fun homeStartFailureEventState(message: String): HomeEventState<Nothing> {
  return HomeEventState.ShowMessage(message)
}

internal fun homeEngineStartedResult(): HomeEngineStartResult<Nothing> {
  return HomeEngineStartResult.Started
}

internal fun <VpnPermissionT> homeEngineVpnPermissionResult(
  permissionRequest: VpnPermissionT
): HomeEngineStartResult<VpnPermissionT> {
  return HomeEngineStartResult.VpnPermissionRequired(permissionRequest)
}

internal fun homeEngineStartFailedResult(message: String): HomeEngineStartResult<Nothing> {
  return HomeEngineStartResult.Failed(message)
}

internal fun <VpnPermissionT> homeEngineStartEventState(
  result: HomeEngineStartResult<VpnPermissionT>
): HomeEventState<VpnPermissionT>? {
  return when (result) {
    HomeEngineStartResult.Started -> null
    is HomeEngineStartResult.VpnPermissionRequired ->
      HomeEventState.RequestVpnPermission(result.permissionRequest)
    is HomeEngineStartResult.Failed -> HomeEventState.ShowMessage(result.message)
  }
}

internal fun homeConsumedEventState(): HomeEventState<Nothing> {
  return HomeEventState.Idle
}

internal fun homeBroadcastAction(
  kind: HomeBroadcastEventKind,
  stoppedMessage: String? = null,
): HomeBroadcastAction {
  return when (kind) {
    HomeBroadcastEventKind.ServiceRecreated,
    HomeBroadcastEventKind.Started,
    HomeBroadcastEventKind.ProfileChanged,
    HomeBroadcastEventKind.ProfileLoaded -> HomeBroadcastAction(shouldFetch = true)
    HomeBroadcastEventKind.Stopped ->
      HomeBroadcastAction(shouldFetch = true, stoppedMessage = stoppedMessage)
    HomeBroadcastEventKind.ProfileUpdateCompleted,
    HomeBroadcastEventKind.ProfileUpdateFailed -> HomeBroadcastAction(shouldFetch = false)
  }
}

internal fun homeToggleAction(clashRunning: Boolean): HomeToggleAction {
  return if (clashRunning) HomeToggleAction.StopClash else HomeToggleAction.StartClash
}

internal fun homeActiveFetchAction(active: Boolean): HomeActiveFetchAction {
  return if (active) HomeActiveFetchAction.RequestFetch else HomeActiveFetchAction.Ignore
}

internal fun homeTrafficPollAction(clashRunning: Boolean): HomeTrafficPollAction {
  return if (clashRunning) HomeTrafficPollAction.QueryTraffic else HomeTrafficPollAction.Ignore
}

internal fun homeTrafficPollAction(
  started: Boolean,
  clashRunning: Boolean,
): HomeTrafficPollAction {
  return if (started) homeTrafficPollAction(clashRunning) else HomeTrafficPollAction.Ignore
}

internal fun homeNoProfileSnackbarAction(
  result: SnackbarActionResult
): HomeNoProfileSnackbarAction {
  return when (result) {
    SnackbarActionResult.ActionPerformed -> HomeNoProfileSnackbarAction.OpenProfiles
    SnackbarActionResult.Dismissed -> HomeNoProfileSnackbarAction.Ignore
  }
}

internal fun homeVpnPermissionResultFromPlatformResultCode(
  resultCode: Int,
  grantedResultCode: Int,
): HomeVpnPermissionResult {
  return if (resultCode == grantedResultCode) {
    HomeVpnPermissionResult.Granted
  } else {
    HomeVpnPermissionResult.Denied
  }
}

internal fun homeVpnPermissionResultAction(
  result: HomeVpnPermissionResult
): HomeVpnPermissionResultAction {
  return when (result) {
    HomeVpnPermissionResult.Granted -> HomeVpnPermissionResultAction.StartEngine
    HomeVpnPermissionResult.Denied -> HomeVpnPermissionResultAction.Ignore
  }
}

internal fun homeModeLabel(mode: TunnelState.Mode): HomeModeLabel {
  return when (mode) {
    TunnelState.Mode.Direct -> HomeModeLabel.Direct
    TunnelState.Mode.Global -> HomeModeLabel.Global
    TunnelState.Mode.Rule -> HomeModeLabel.Rule
  }
}

internal fun <T> homeModeLabelPlatformToken(
  label: HomeModeLabel,
  directMode: T,
  globalMode: T,
  ruleMode: T,
): T {
  return when (label) {
    HomeModeLabel.Direct -> directMode
    HomeModeLabel.Global -> globalMode
    HomeModeLabel.Rule -> ruleMode
  }
}

internal fun homeFetchedPlatformState(
  clashRunning: Boolean,
  mode: String,
  hasProviders: Boolean,
  profileName: String?,
): HomeFetchedPlatformState {
  return HomeFetchedPlatformState(
    clashRunning = clashRunning,
    mode = mode,
    hasProviders = hasProviders,
    profileName = profileName,
  )
}

internal fun HomeUiState.withFetchedHomeState(
  platformState: HomeFetchedPlatformState
): HomeUiState {
  return withFetchedHomeState(
    clashRunning = platformState.clashRunning,
    mode = platformState.mode,
    hasProviders = platformState.hasProviders,
    profileName = platformState.profileName,
  )
}

internal fun HomeUiState.withFetchedHomeState(
  clashRunning: Boolean,
  mode: String,
  hasProviders: Boolean,
  profileName: String?,
): HomeUiState {
  return copy(
    mode = if (clashRunning) mode else null,
    hasProviders = clashRunning && hasProviders,
    profileName = profileName,
  )
}

internal fun HomeUiState.withForwardedTraffic(forwarded: String): HomeUiState {
  return copy(forwarded = forwarded)
}
