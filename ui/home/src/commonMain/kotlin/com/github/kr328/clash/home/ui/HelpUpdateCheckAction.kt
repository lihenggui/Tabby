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
