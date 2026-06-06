package com.github.kr328.clash.engine.desktop

import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.core.model.ProxyGroup
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.core.model.Traffic
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.engine.api.EngineController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DesktopEngineController(
  private val mihomoProcess: DesktopMihomoProcess? = null,
  private val mihomoApi: DesktopMihomoApi? = null,
) : EngineController {
  private val _state = MutableStateFlow(TunnelState(TunnelState.Mode.Rule))

  override val state: StateFlow<TunnelState> = _state.asStateFlow()

  override suspend fun start() {
    (mihomoProcess ?: unsupported()).start()
  }

  override suspend fun stop() {
    (mihomoProcess ?: unsupported()).stop()
  }

  override suspend fun queryTraffic(): Traffic {
    return (mihomoApi ?: unsupported()).queryTraffic()
  }

  override suspend fun queryProviders(): List<Provider> {
    return (mihomoApi ?: unsupported()).queryProviders()
  }

  override suspend fun updateProvider(type: Provider.Type, name: String) {
    (mihomoApi ?: unsupported()).updateProvider(type, name)
  }

  override suspend fun queryProxyGroupNames(excludeNotSelectable: Boolean): List<String> {
    return (mihomoApi ?: unsupported()).queryProxyGroupNames(excludeNotSelectable)
  }

  override suspend fun queryProxyGroup(name: String, sort: ProxySort): ProxyGroup {
    return (mihomoApi ?: unsupported()).queryProxyGroup(name, sort)
  }

  override suspend fun patchSelector(group: String, proxy: String): Boolean {
    return (mihomoApi ?: unsupported()).patchSelector(group, proxy)
  }

  override suspend fun healthCheck(group: String) {
    (mihomoApi ?: unsupported()).healthCheck(group)
  }

  override suspend fun healthCheckProxy(group: String, proxy: String) {
    (mihomoApi ?: unsupported()).healthCheckProxy(proxy)
  }

  override suspend fun querySessionMode(): TunnelState.Mode? {
    return (mihomoApi ?: unsupported()).queryMode()
  }

  override suspend fun patchSessionMode(mode: TunnelState.Mode?) {
    (mihomoApi ?: unsupported()).patchMode(
      requireNotNull(mode) { "Desktop session mode requires a concrete mode" }
    )
  }
}

class DesktopEngineUnsupportedException :
  UnsupportedOperationException("Desktop engine implementation is not available yet")

internal fun unsupported(): Nothing {
  throw DesktopEngineUnsupportedException()
}
