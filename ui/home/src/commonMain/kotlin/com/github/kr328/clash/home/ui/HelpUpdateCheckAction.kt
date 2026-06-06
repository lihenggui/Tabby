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

internal enum class HelpUpdateAvailableSnackbarAction {
  OpenReleases,
  Ignore,
}

internal sealed interface HelpEventState {
  data object Idle : HelpEventState

  data class ShowMessage(val message: String) : HelpEventState

  data class UpdateAvailable(val releasesUrl: String) : HelpEventState
}

internal fun helpInitialEventState(): HelpEventState {
  return HelpEventState.Idle
}

internal fun helpUpdateCheckRequestAction(state: HelpContentState): HelpUpdateCheckRequestAction {
  return if (state.checkingForUpdates) {
    HelpUpdateCheckRequestAction.Ignore
  } else {
    HelpUpdateCheckRequestAction.StartCheck
  }
}

internal fun helpUpdateCheckAction(
  latestTag: String?,
  localVersion: String,
): HelpUpdateCheckAction {
  val latestVersion = latestTag ?: return HelpUpdateCheckAction.ShowUpdateCheckFailedMessage

  return if (SemVer.parse(latestVersion) > SemVer.parse(localVersion)) {
    HelpUpdateCheckAction.ShowUpdateAvailable
  } else {
    HelpUpdateCheckAction.ShowAlreadyUpToDateMessage
  }
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
