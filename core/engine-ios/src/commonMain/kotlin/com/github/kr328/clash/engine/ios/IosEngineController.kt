package com.github.kr328.clash.engine.ios

import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.core.model.ProxyGroup
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.core.model.Traffic
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.engine.api.EngineController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class IosEngineController : EngineController {
  private val _state = MutableStateFlow(TunnelState(TunnelState.Mode.Rule))

  override val state: StateFlow<TunnelState> = _state.asStateFlow()

  override suspend fun start() {
    unsupported()
  }

  override suspend fun stop() {
    unsupported()
  }

  override suspend fun queryTraffic(): Traffic {
    unsupported()
  }

  override suspend fun queryProviders(): List<Provider> {
    unsupported()
  }

  override suspend fun updateProvider(type: Provider.Type, name: String) {
    unsupported()
  }

  override suspend fun queryProxyGroupNames(excludeNotSelectable: Boolean): List<String> {
    unsupported()
  }

  override suspend fun queryProxyGroup(name: String, sort: ProxySort): ProxyGroup {
    unsupported()
  }

  override suspend fun patchSelector(group: String, proxy: String): Boolean {
    unsupported()
  }

  override suspend fun healthCheck(group: String) {
    unsupported()
  }

  override suspend fun healthCheckProxy(group: String, proxy: String) {
    unsupported()
  }

  override suspend fun querySessionMode(): TunnelState.Mode? {
    unsupported()
  }

  override suspend fun patchSessionMode(mode: TunnelState.Mode?) {
    unsupported()
  }
}

class IosEngineUnsupportedException :
  UnsupportedOperationException("iOS engine implementation is not available yet")

internal fun unsupported(): Nothing {
  throw IosEngineUnsupportedException()
}
