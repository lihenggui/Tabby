package com.github.kr328.clash.service.remote

import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.kaidl.BinderInterface

@BinderInterface
interface IClashManager {
  fun queryTunnelState(): String

  fun queryTrafficTotal(): Long

  fun queryProxyGroupNames(excludeNotSelectable: Boolean): List<String>

  fun queryProxyGroup(name: String, proxySort: ProxySort): String

  fun queryConfiguration(): String

  fun queryProviders(): String

  fun patchSelector(group: String, name: String): Boolean

  suspend fun healthCheck(group: String)

  suspend fun healthCheckProxy(group: String, name: String)

  suspend fun updateProvider(type: Provider.Type, name: String)

  fun queryOverride(slot: Clash.OverrideSlot): String

  fun patchOverride(slot: Clash.OverrideSlot, configuration: String)

  fun clearOverride(slot: Clash.OverrideSlot)

  fun setLogObserver(observer: ILogObserver?)
}
