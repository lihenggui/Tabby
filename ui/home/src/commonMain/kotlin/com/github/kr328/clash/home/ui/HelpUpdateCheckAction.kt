package com.github.kr328.clash.home.ui

import net.swiftzer.semver.SemVer

internal sealed interface HelpUpdateCheckAction {
  data object ShowUpdateCheckFailedMessage : HelpUpdateCheckAction

  data object ShowAlreadyUpToDateMessage : HelpUpdateCheckAction

  data object ShowUpdateAvailable : HelpUpdateCheckAction
}

internal sealed interface HelpUpdateCheckRequestAction {
  data object StartCheck : HelpUpdateCheckRequestAction

  data object Ignore : HelpUpdateCheckRequestAction
}

internal enum class HelpLocalVersionLoadAction {
  Load,
  Ignore,
}

internal enum class HelpUpdateAvailableSnackbarAction {
  OpenReleases,
  Ignore,
}

internal sealed interface HelpEventState {
  data object Idle : HelpEventState

  data class ShowMessage(val message: String) : HelpEventState

  data class UpdateAvailable(val releasesUrl: String) : HelpEventState
}

internal sealed interface HelpEventRouteEffect {
  data object Ignore : HelpEventRouteEffect

  data class ShowMessage(val message: String) : HelpEventRouteEffect

  data class ShowUpdateAvailable(val releasesUrl: String) : HelpEventRouteEffect
}

internal fun helpInitialEventState(): HelpEventState {
  return HelpEventState.Idle
}

internal fun helpEventRouteEffect(eventState: HelpEventState): HelpEventRouteEffect {
  return when (eventState) {
    HelpEventState.Idle -> HelpEventRouteEffect.Ignore
    is HelpEventState.ShowMessage -> HelpEventRouteEffect.ShowMessage(eventState.message)
    is HelpEventState.UpdateAvailable ->
      HelpEventRouteEffect.ShowUpdateAvailable(eventState.releasesUrl)
  }
}

internal fun helpUpdateCheckRequestAction(state: HelpContentState): HelpUpdateCheckRequestAction {
  return if (state.checkingForUpdates) {
    HelpUpdateCheckRequestAction.Ignore
  } else {
    HelpUpdateCheckRequestAction.StartCheck
  }
}

internal fun helpLocalVersionLoadAction(latestTag: String?): HelpLocalVersionLoadAction {
  return if (latestTag == null) {
    HelpLocalVersionLoadAction.Ignore
  } else {
    HelpLocalVersionLoadAction.Load
  }
}

internal fun helpUpdateCheckAction(
  latestTag: String?,
  localVersion: String?,
): HelpUpdateCheckAction {
  val latestVersion =
    latestTag?.toSemVerOrNull() ?: return HelpUpdateCheckAction.ShowUpdateCheckFailedMessage
  val currentVersion =
    localVersion?.toSemVerOrNull() ?: return HelpUpdateCheckAction.ShowUpdateCheckFailedMessage

  return if (latestVersion > currentVersion) {
    HelpUpdateCheckAction.ShowUpdateAvailable
  } else {
    HelpUpdateCheckAction.ShowAlreadyUpToDateMessage
  }
}

private fun String.toSemVerOrNull(): SemVer? {
  return runCatching { SemVer.parse(this) }.getOrNull()
}

internal fun helpUpdateCheckEventState(
  action: HelpUpdateCheckAction,
  releasesUrl: String,
  alreadyUpToDateMessage: String,
  updateCheckFailedMessage: String,
): HelpEventState {
  return when (action) {
    HelpUpdateCheckAction.ShowUpdateAvailable -> HelpEventState.UpdateAvailable(releasesUrl)
    HelpUpdateCheckAction.ShowAlreadyUpToDateMessage ->
      HelpEventState.ShowMessage(alreadyUpToDateMessage)
    HelpUpdateCheckAction.ShowUpdateCheckFailedMessage ->
      HelpEventState.ShowMessage(updateCheckFailedMessage)
  }
}

internal fun helpUpdateCheckFailureEventState(updateCheckFailedMessage: String): HelpEventState {
  return HelpEventState.ShowMessage(updateCheckFailedMessage)
}

internal fun helpUpdateAvailableSnackbarAction(
  result: SnackbarActionResult
): HelpUpdateAvailableSnackbarAction {
  return when (result) {
    SnackbarActionResult.ActionPerformed -> HelpUpdateAvailableSnackbarAction.OpenReleases
    SnackbarActionResult.Dismissed -> HelpUpdateAvailableSnackbarAction.Ignore
  }
}

internal fun helpConsumedEventState(): HelpEventState {
  return HelpEventState.Idle
}
