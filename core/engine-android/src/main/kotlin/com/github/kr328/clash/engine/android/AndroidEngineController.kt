package com.github.kr328.clash.engine.android

import android.content.Context
import android.content.Intent
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.core.model.ProxyGroup
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.core.model.Traffic
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.engine.api.EngineController
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.util.startClashService
import com.github.kr328.clash.glue.util.stopClashService
import com.github.kr328.clash.glue.util.withClash
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AndroidEngineController(
  private val context: Context,
  private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : EngineController {
  private val _state = MutableStateFlow(TunnelState(TunnelState.Mode.Rule))

  override val state: StateFlow<TunnelState> = _state.asStateFlow()

  init {
    scope.launch { refreshState() }
    scope.launch {
      Remote.broadcasts.register()
      Remote.broadcasts.event.collect { refreshState() }
    }
  }

  override suspend fun start() {
    context.startClashService()?.let { throw VpnPermissionRequiredException(it) }
    refreshState()
  }

  override suspend fun stop() {
    context.stopClashService()
  }

  override suspend fun queryState(): TunnelState {
    return withClash { queryTunnelState() }
  }

  override suspend fun queryTraffic(): Traffic {
    return withClash { queryTrafficTotal() }
  }

  override suspend fun queryProviders(): List<Provider> {
    return withClash { queryProviders() }
  }

  override suspend fun updateProvider(type: Provider.Type, name: String) {
    withClash { updateProvider(type, name) }
  }

  override suspend fun queryProxyGroupNames(excludeNotSelectable: Boolean): List<String> {
    return withClash { queryProxyGroupNames(excludeNotSelectable) }
  }

  override suspend fun queryProxyGroup(name: String, sort: ProxySort): ProxyGroup {
    return withClash { queryProxyGroup(name, sort) }
  }

  override suspend fun patchSelector(group: String, proxy: String): Boolean {
    return withClash { patchSelector(group, proxy) }
  }

  override suspend fun healthCheck(group: String) {
    withClash { healthCheck(group) }
  }

  override suspend fun healthCheckProxy(group: String, proxy: String) {
    withClash { healthCheckProxy(group, proxy) }
  }

  override suspend fun querySessionMode(): TunnelState.Mode? {
    return withClash { queryOverride(Clash.OverrideSlot.Session).mode }
  }

  override suspend fun patchSessionMode(mode: TunnelState.Mode?) {
    withClash {
      val override = queryOverride(Clash.OverrideSlot.Session)
      patchOverride(Clash.OverrideSlot.Session, override.copy(mode = mode))
    }
  }

  override suspend fun queryPersistOverride(): ConfigurationOverride {
    return withClash { queryOverride(Clash.OverrideSlot.Persist) }
  }

  override suspend fun patchPersistOverride(configuration: ConfigurationOverride) {
    withClash { patchOverride(Clash.OverrideSlot.Persist, configuration) }
  }

  override suspend fun clearPersistOverride() {
    withClash { clearOverride(Clash.OverrideSlot.Persist) }
  }

  private suspend fun refreshState() {
    runCatching { queryState() }.onSuccess { _state.value = it }
  }
}

class VpnPermissionRequiredException(val prepareIntent: Intent) :
  IllegalStateException("VPN permission is required before starting the Android engine")
