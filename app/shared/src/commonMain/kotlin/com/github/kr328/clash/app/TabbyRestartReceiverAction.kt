package com.github.kr328.clash.app

sealed interface TabbyRestartReceiverEvent {
  data object BootCompleted : TabbyRestartReceiverEvent

  data object PackageReplaced : TabbyRestartReceiverEvent
}

sealed interface TabbyRestartReceiverAction {
  data object StartClash : TabbyRestartReceiverAction

  data object Ignore : TabbyRestartReceiverAction
}

fun tabbyRestartReceiverEventFromString(
  action: String?,
  bootCompletedBroadcastAction: String,
  packageReplacedBroadcastAction: String,
): TabbyRestartReceiverEvent? =
  when (action) {
    bootCompletedBroadcastAction -> TabbyRestartReceiverEvent.BootCompleted
    packageReplacedBroadcastAction -> TabbyRestartReceiverEvent.PackageReplaced
    else -> null
  }

fun tabbyRestartReceiverAction(
  event: TabbyRestartReceiverEvent?,
  shouldStartClashOnBoot: Boolean,
): TabbyRestartReceiverAction =
  when {
    event == null -> TabbyRestartReceiverAction.Ignore
    shouldStartClashOnBoot -> TabbyRestartReceiverAction.StartClash
    else -> TabbyRestartReceiverAction.Ignore
  }
