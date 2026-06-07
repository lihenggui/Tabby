package com.github.kr328.clash.app

sealed interface TabbyRestartReceiverEvent {
  data object BootCompleted : TabbyRestartReceiverEvent

  data object PackageReplaced : TabbyRestartReceiverEvent
}

sealed interface TabbyRestartReceiverAction {
  data object StartClash : TabbyRestartReceiverAction

  data object Ignore : TabbyRestartReceiverAction
}

data class TabbyRestartReceiverPlatformSpec(val startClash: Boolean)

fun tabbyRestartReceiverEventFromString(
  action: String?,
  bootCompletedAction: String,
  packageReplacedAction: String,
): TabbyRestartReceiverEvent? =
  when (action) {
    bootCompletedAction -> TabbyRestartReceiverEvent.BootCompleted
    packageReplacedAction -> TabbyRestartReceiverEvent.PackageReplaced
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

fun tabbyRestartReceiverPlatformSpec(
  action: TabbyRestartReceiverAction
): TabbyRestartReceiverPlatformSpec =
  when (action) {
    TabbyRestartReceiverAction.StartClash -> TabbyRestartReceiverPlatformSpec(startClash = true)
    TabbyRestartReceiverAction.Ignore -> TabbyRestartReceiverPlatformSpec(startClash = false)
  }
