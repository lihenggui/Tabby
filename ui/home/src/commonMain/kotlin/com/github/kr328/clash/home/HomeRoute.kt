package com.github.kr328.clash.home

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface HomeRoute : NavKey {
  @Serializable data object Home : HomeRoute

  @Serializable data object Help : HomeRoute
}
