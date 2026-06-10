package com.github.kr328.clash.app

data class TabbyTileState(
  val clashRunning: Boolean = false,
  val currentProfile: String = "",
)

sealed interface TabbyTileEvent {
  data object ClashStarted : TabbyTileEvent

  data object ClashStopped : TabbyTileEvent

  data object ServiceRecreated : TabbyTileEvent

  data class ProfileLoaded(val profileName: String?) : TabbyTileEvent
}

sealed interface TabbyTileBroadcastAction {
  data object ClashStarted : TabbyTileBroadcastAction

  data object ClashStopped : TabbyTileBroadcastAction

  data object ServiceRecreated : TabbyTileBroadcastAction

  data object ProfileLoaded : TabbyTileBroadcastAction
}

sealed interface TabbyTileBroadcastPlan {
  data class Reduce(val event: TabbyTileEvent) : TabbyTileBroadcastPlan

  data object LoadCurrentProfile : TabbyTileBroadcastPlan

  data object Ignore : TabbyTileBroadcastPlan
}

sealed interface TabbyTileClickState {
  data object Active : TabbyTileClickState

  data object Inactive : TabbyTileClickState

  data object Other : TabbyTileClickState
}

sealed interface TabbyTileClickAction {
  data object StartClash : TabbyTileClickAction

  data object StopClash : TabbyTileClickAction

  data object Ignore : TabbyTileClickAction
}

data class TabbyTilePresentation(
  val active: Boolean,
  val profileName: String?,
)

fun tabbyTileBroadcastActionFromString(
  action: String?,
  clashStartedAction: String,
  clashStoppedAction: String,
  serviceRecreatedAction: String,
  profileLoadedAction: String,
): TabbyTileBroadcastAction? =
  when (action) {
    clashStartedAction -> TabbyTileBroadcastAction.ClashStarted
    clashStoppedAction -> TabbyTileBroadcastAction.ClashStopped
    serviceRecreatedAction -> TabbyTileBroadcastAction.ServiceRecreated
    profileLoadedAction -> TabbyTileBroadcastAction.ProfileLoaded
    else -> null
  }

fun tabbyTileInitialState(currentProfile: String?): TabbyTileState =
  TabbyTileState(
    clashRunning = currentProfile != null,
    currentProfile = currentProfile.orEmpty(),
  )

fun reduceTabbyTileState(
  state: TabbyTileState,
  event: TabbyTileEvent,
): TabbyTileState =
  when (event) {
    TabbyTileEvent.ClashStarted ->
      state.copy(
        clashRunning = true,
        currentProfile = "",
      )
    TabbyTileEvent.ClashStopped,
    TabbyTileEvent.ServiceRecreated ->
      state.copy(
        clashRunning = false,
        currentProfile = "",
      )
    is TabbyTileEvent.ProfileLoaded -> state.copy(currentProfile = event.profileName.orEmpty())
  }

fun tabbyTileClickAction(clickState: TabbyTileClickState?): TabbyTileClickAction =
  when (clickState) {
    TabbyTileClickState.Active -> TabbyTileClickAction.StopClash
    TabbyTileClickState.Inactive -> TabbyTileClickAction.StartClash
    TabbyTileClickState.Other,
    null -> TabbyTileClickAction.Ignore
  }

fun tabbyTileClickStateFromPlatformState(
  platformState: Int?,
  activePlatformState: Int,
  inactivePlatformState: Int,
): TabbyTileClickState? =
  when (platformState) {
    activePlatformState -> TabbyTileClickState.Active
    inactivePlatformState -> TabbyTileClickState.Inactive
    null -> null
    else -> TabbyTileClickState.Other
  }

fun tabbyTilePresentation(state: TabbyTileState): TabbyTilePresentation =
  TabbyTilePresentation(
    active = state.clashRunning,
    profileName = state.currentProfile.ifEmpty { null },
  )

fun tabbyTilePresentationPlatformState(
  presentation: TabbyTilePresentation,
  activePlatformState: Int,
  inactivePlatformState: Int,
): Int = if (presentation.active) activePlatformState else inactivePlatformState

fun tabbyTilePresentationLabel(
  presentation: TabbyTilePresentation,
  defaultLabel: String,
): String {
  return presentation.profileName ?: defaultLabel
}

fun tabbyTileBroadcastPlan(action: TabbyTileBroadcastAction?): TabbyTileBroadcastPlan =
  when (action) {
    TabbyTileBroadcastAction.ClashStarted ->
      TabbyTileBroadcastPlan.Reduce(TabbyTileEvent.ClashStarted)
    TabbyTileBroadcastAction.ClashStopped ->
      TabbyTileBroadcastPlan.Reduce(TabbyTileEvent.ClashStopped)
    TabbyTileBroadcastAction.ServiceRecreated ->
      TabbyTileBroadcastPlan.Reduce(TabbyTileEvent.ServiceRecreated)
    TabbyTileBroadcastAction.ProfileLoaded -> TabbyTileBroadcastPlan.LoadCurrentProfile
    null -> TabbyTileBroadcastPlan.Ignore
  }

fun tabbyTileProfileLoadedEvent(currentProfile: String?): TabbyTileEvent =
  TabbyTileEvent.ProfileLoaded(currentProfile)
