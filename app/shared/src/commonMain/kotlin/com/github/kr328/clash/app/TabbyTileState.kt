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

fun tabbyTileClickAction(clickState: TabbyTileClickState): TabbyTileClickAction =
  when (clickState) {
    TabbyTileClickState.Active -> TabbyTileClickAction.StopClash
    TabbyTileClickState.Inactive -> TabbyTileClickAction.StartClash
    TabbyTileClickState.Other -> TabbyTileClickAction.Ignore
  }

fun tabbyTilePresentation(state: TabbyTileState): TabbyTilePresentation =
  TabbyTilePresentation(
    active = state.clashRunning,
    profileName = state.currentProfile.ifEmpty { null },
  )
