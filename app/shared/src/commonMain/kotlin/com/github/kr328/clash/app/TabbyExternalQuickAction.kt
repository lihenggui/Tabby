package com.github.kr328.clash.app

sealed interface TabbyExternalQuickAction {
  data object ToggleClash : TabbyExternalQuickAction

  data object StartClash : TabbyExternalQuickAction

  data object StopClash : TabbyExternalQuickAction
}

sealed interface TabbyExternalQuickActionPlan {
  data object StartClash : TabbyExternalQuickActionPlan

  data object StopClash : TabbyExternalQuickActionPlan

  data object ShowAlreadyStarted : TabbyExternalQuickActionPlan

  data object ShowAlreadyStopped : TabbyExternalQuickActionPlan
}

sealed interface TabbyStartClashResultAction {
  data object ShowVpnPermissionRequired : TabbyStartClashResultAction

  data object ShowStarted : TabbyStartClashResultAction
}

sealed interface TabbyStopClashResultAction {
  data object ShowStopped : TabbyStopClashResultAction
}

fun tabbyExternalQuickActionPlan(
  action: TabbyExternalQuickAction,
  clashRunning: Boolean,
): TabbyExternalQuickActionPlan =
  when (action) {
    TabbyExternalQuickAction.ToggleClash ->
      if (clashRunning) TabbyExternalQuickActionPlan.StopClash
      else TabbyExternalQuickActionPlan.StartClash
    TabbyExternalQuickAction.StartClash ->
      if (clashRunning) TabbyExternalQuickActionPlan.ShowAlreadyStarted
      else TabbyExternalQuickActionPlan.StartClash
    TabbyExternalQuickAction.StopClash ->
      if (clashRunning) TabbyExternalQuickActionPlan.StopClash
      else TabbyExternalQuickActionPlan.ShowAlreadyStopped
  }

fun tabbyStartClashResultAction(vpnPermissionRequired: Boolean): TabbyStartClashResultAction =
  if (vpnPermissionRequired) {
    TabbyStartClashResultAction.ShowVpnPermissionRequired
  } else {
    TabbyStartClashResultAction.ShowStarted
  }

fun tabbyStopClashResultAction(): TabbyStopClashResultAction =
  TabbyStopClashResultAction.ShowStopped
