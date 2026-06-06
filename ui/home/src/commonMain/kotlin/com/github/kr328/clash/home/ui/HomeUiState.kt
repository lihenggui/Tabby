package com.github.kr328.clash.home.ui

import com.github.kr328.clash.core.model.Profile

internal data class HomeUiState(
  val forwarded: String? = null,
  val mode: String? = null,
  val profileName: String? = null,
  val hasProviders: Boolean = false,
)

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

internal data class HomeBroadcastAction(
  val shouldFetch: Boolean,
  val stoppedMessage: String? = null,
)

internal enum class HomeToggleAction {
  StartClash,
  StopClash,
}

internal enum class HomeTrafficPollAction {
  QueryTraffic,
  Ignore,
}

internal fun homeStartAction(activeProfile: Profile?): HomeStartAction {
  return if (activeProfile?.imported == true) HomeStartAction.StartEngine
  else HomeStartAction.ShowNoProfileMessage
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

internal fun homeTrafficPollAction(clashRunning: Boolean): HomeTrafficPollAction {
  return if (clashRunning) HomeTrafficPollAction.QueryTraffic else HomeTrafficPollAction.Ignore
}

internal fun HomeUiState.withFetchedHomeState(
  clashRunning: Boolean,
  mode: String,
  hasProviders: Boolean,
  profileName: String?,
): HomeUiState {
  return copy(
    mode = if (clashRunning) mode else null,
    hasProviders = hasProviders,
    profileName = profileName,
  )
}

internal fun HomeUiState.withForwardedTraffic(forwarded: String): HomeUiState {
  return copy(forwarded = forwarded)
}
