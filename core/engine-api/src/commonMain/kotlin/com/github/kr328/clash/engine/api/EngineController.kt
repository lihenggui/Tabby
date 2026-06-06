package com.github.kr328.clash.engine.api

import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.core.model.ProxyGroup
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.core.model.Traffic
import com.github.kr328.clash.core.model.TunnelState
import kotlinx.coroutines.flow.StateFlow

interface EngineController {
  val state: StateFlow<TunnelState>

  suspend fun start()

  suspend fun stop()

  suspend fun queryState(): TunnelState

  suspend fun queryTraffic(): Traffic

  suspend fun queryProviders(): List<Provider>

  suspend fun updateProvider(type: Provider.Type, name: String)

  suspend fun queryProxyGroupNames(excludeNotSelectable: Boolean): List<String>

  suspend fun queryProxyGroup(name: String, sort: ProxySort): ProxyGroup

  suspend fun patchSelector(group: String, proxy: String): Boolean

  suspend fun healthCheck(group: String)

  suspend fun healthCheckProxy(group: String, proxy: String)

  suspend fun querySessionMode(): TunnelState.Mode?

  suspend fun patchSessionMode(mode: TunnelState.Mode?)
}
