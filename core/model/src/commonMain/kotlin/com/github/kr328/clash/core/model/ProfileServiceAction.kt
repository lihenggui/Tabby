package com.github.kr328.clash.core.model

enum class ProfileReceiverStartAction {
  ScheduleUpdates,
  RequestUpdate,
  Ignore,
}

enum class ProfileWorkerStartAction {
  RequestUpdate,
  ScheduleUpdates,
  Ignore,
}

fun profileReceiverStartAction(
  action: String?,
  scheduleTriggerActions: Set<String>,
  requestUpdateAction: String,
): ProfileReceiverStartAction {
  return when (action) {
    in scheduleTriggerActions -> ProfileReceiverStartAction.ScheduleUpdates
    requestUpdateAction -> ProfileReceiverStartAction.RequestUpdate
    else -> ProfileReceiverStartAction.Ignore
  }
}

fun profileWorkerStartAction(
  action: String?,
  requestUpdateAction: String,
  scheduleUpdatesAction: String,
): ProfileWorkerStartAction {
  return when (action) {
    requestUpdateAction -> ProfileWorkerStartAction.RequestUpdate
    scheduleUpdatesAction -> ProfileWorkerStartAction.ScheduleUpdates
    else -> ProfileWorkerStartAction.Ignore
  }
}
