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
