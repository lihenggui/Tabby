package com.github.kr328.clash.proxy

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface ProxyRoute : NavKey {
  @Serializable data object Proxy : ProxyRoute
}
