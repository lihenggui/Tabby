package com.github.kr328.clash.glue.remote

sealed interface ClashServiceState {
  data object Stopped : ClashServiceState

  data object Loading : ClashServiceState

  data object Running : ClashServiceState
}

internal fun clashServiceStateFromCurrentProfileResult(
  hasResult: Boolean,
  profileName: String?,
): ClashServiceState =
  when {
    !hasResult -> ClashServiceState.Stopped
    profileName == null -> ClashServiceState.Loading
    else -> ClashServiceState.Running
  }
