package com.github.kr328.clash.common.service

sealed interface TabbyStatusProviderCallAction {
  data class CurrentProfile(val name: String?) : TabbyStatusProviderCallAction

  data object NoResult : TabbyStatusProviderCallAction

  data object Delegate : TabbyStatusProviderCallAction
}

fun tabbyStatusProviderCallAction(
  method: String,
  currentProfileMethod: String,
  serviceRunning: Boolean,
  currentProfileName: String?,
): TabbyStatusProviderCallAction {
  return when (method) {
    currentProfileMethod ->
      if (serviceRunning) {
        TabbyStatusProviderCallAction.CurrentProfile(name = currentProfileName)
      } else {
        TabbyStatusProviderCallAction.NoResult
      }
    else -> TabbyStatusProviderCallAction.Delegate
  }
}
