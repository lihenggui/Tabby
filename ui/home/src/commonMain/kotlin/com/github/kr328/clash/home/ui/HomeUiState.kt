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

internal fun homeStartAction(activeProfile: Profile?): HomeStartAction {
  return if (activeProfile?.imported == true) HomeStartAction.StartEngine
  else HomeStartAction.ShowNoProfileMessage
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
