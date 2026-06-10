package com.github.kr328.clash.common.remote

enum class TabbyRemoteBroadcastAction {
  ServiceRecreated,
  ClashStarted,
  ClashStopped,
  ProfileChanged,
  ProfileUpdateCompleted,
  ProfileUpdateFailed,
  ProfileLoaded,
}

fun tabbyRemoteBroadcastActionFromPlatformAction(
  action: String?,
  serviceRecreatedAction: String,
  clashStartedAction: String,
  clashStoppedAction: String,
  profileChangedAction: String,
  profileUpdateCompletedAction: String,
  profileUpdateFailedAction: String,
  profileLoadedAction: String,
): TabbyRemoteBroadcastAction? {
  return when (action) {
    serviceRecreatedAction -> TabbyRemoteBroadcastAction.ServiceRecreated
    clashStartedAction -> TabbyRemoteBroadcastAction.ClashStarted
    clashStoppedAction -> TabbyRemoteBroadcastAction.ClashStopped
    profileChangedAction -> TabbyRemoteBroadcastAction.ProfileChanged
    profileUpdateCompletedAction -> TabbyRemoteBroadcastAction.ProfileUpdateCompleted
    profileUpdateFailedAction -> TabbyRemoteBroadcastAction.ProfileUpdateFailed
    profileLoadedAction -> TabbyRemoteBroadcastAction.ProfileLoaded
    else -> null
  }
}

fun tabbyRemoteBroadcastClashRunningState(action: TabbyRemoteBroadcastAction): Boolean? {
  return when (action) {
    TabbyRemoteBroadcastAction.ServiceRecreated,
    TabbyRemoteBroadcastAction.ClashStopped -> false
    TabbyRemoteBroadcastAction.ClashStarted -> true
    TabbyRemoteBroadcastAction.ProfileChanged,
    TabbyRemoteBroadcastAction.ProfileUpdateCompleted,
    TabbyRemoteBroadcastAction.ProfileUpdateFailed,
    TabbyRemoteBroadcastAction.ProfileLoaded -> null
  }
}
