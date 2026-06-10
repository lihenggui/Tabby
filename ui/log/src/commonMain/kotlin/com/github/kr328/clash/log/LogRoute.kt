package com.github.kr328.clash.log

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface LogRoute : NavKey {
  @Serializable data object Root : LogRoute

  @Serializable data object Logs : LogRoute

  @Serializable data class Logcat(val fileName: String? = null) : LogRoute
}
