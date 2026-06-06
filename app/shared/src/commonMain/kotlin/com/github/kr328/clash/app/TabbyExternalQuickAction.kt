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

sealed interface TabbyExternalQuickActionHandlingPlan {
  data class Handle(val actionPlan: TabbyExternalQuickActionPlan) :
    TabbyExternalQuickActionHandlingPlan

  data object Ignore : TabbyExternalQuickActionHandlingPlan
}

sealed interface TabbyStartClashResultAction {
  data object ShowVpnPermissionRequired : TabbyStartClashResultAction

  data object ShowStarted : TabbyStartClashResultAction
}

sealed interface TabbyStopClashResultAction {
  data object ShowStopped : TabbyStopClashResultAction
}

fun tabbyExternalQuickActionString(
  action: TabbyExternalQuickAction,
  toggleClashAction: String,
  startClashAction: String,
  stopClashAction: String,
): String =
  when (action) {
    TabbyExternalQuickAction.ToggleClash -> toggleClashAction
    TabbyExternalQuickAction.StartClash -> startClashAction
    TabbyExternalQuickAction.StopClash -> stopClashAction
  }

fun tabbyExternalQuickActionFromString(
  action: String?,
  toggleClashAction: String,
  startClashAction: String,
  stopClashAction: String,
): TabbyExternalQuickAction? =
  when (action) {
    toggleClashAction -> TabbyExternalQuickAction.ToggleClash
    startClashAction -> TabbyExternalQuickAction.StartClash
    stopClashAction -> TabbyExternalQuickAction.StopClash
    else -> null
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

fun tabbyExternalQuickActionHandlingPlan(
  action: TabbyExternalQuickAction?,
  clashRunning: Boolean,
): TabbyExternalQuickActionHandlingPlan =
  action?.let {
    TabbyExternalQuickActionHandlingPlan.Handle(
      tabbyExternalQuickActionPlan(
        action = it,
        clashRunning = clashRunning,
      )
    )
  } ?: TabbyExternalQuickActionHandlingPlan.Ignore

fun tabbyStartClashResultAction(vpnPermissionRequired: Boolean): TabbyStartClashResultAction =
  if (vpnPermissionRequired) {
    TabbyStartClashResultAction.ShowVpnPermissionRequired
  } else {
    TabbyStartClashResultAction.ShowStarted
  }

fun tabbyStopClashResultAction(): TabbyStopClashResultAction =
  TabbyStopClashResultAction.ShowStopped
