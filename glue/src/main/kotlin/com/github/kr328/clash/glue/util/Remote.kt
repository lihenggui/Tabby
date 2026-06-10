package com.github.kr328.clash.glue.util

import android.os.DeadObjectException
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.core.model.FetchStatus
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.core.model.ProxyGroup
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.core.model.Traffic
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.core.model.UiConfiguration
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.service.remote.IClashManager
import com.github.kr328.clash.service.remote.IFetchObserver
import com.github.kr328.clash.service.remote.IProfileManager
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ClashClient(private val remote: IClashManager) {
  fun queryTunnelState(): TunnelState {
    return json.decodeFromString(remote.queryTunnelState())
  }

  fun queryTrafficTotal(): Traffic {
    return Traffic(remote.queryTrafficTotal())
  }

  fun queryProxyGroupNames(excludeNotSelectable: Boolean): List<String> {
    return remote.queryProxyGroupNames(excludeNotSelectable)
  }

  fun queryProxyGroup(name: String, proxySort: ProxySort): ProxyGroup {
    return json.decodeFromString(remote.queryProxyGroup(name, proxySort))
  }

  fun queryConfiguration(): UiConfiguration {
    return json.decodeFromString(remote.queryConfiguration())
  }

  fun queryProviders(): List<Provider> {
    return json.decodeFromString(remote.queryProviders())
  }

  fun patchSelector(group: String, name: String): Boolean {
    return remote.patchSelector(group, name)
  }

  suspend fun healthCheck(group: String) {
    remote.healthCheck(group)
  }

  suspend fun healthCheckProxy(group: String, name: String) {
    remote.healthCheckProxy(group, name)
  }

  suspend fun updateProvider(type: Provider.Type, name: String) {
    remote.updateProvider(type, name)
  }

  fun queryOverride(slot: Clash.OverrideSlot): ConfigurationOverride {
    return json.decodeFromString(remote.queryOverride(slot))
  }

  fun patchOverride(slot: Clash.OverrideSlot, configuration: ConfigurationOverride) {
    remote.patchOverride(slot, json.encodeToString(configuration))
  }

  fun clearOverride(slot: Clash.OverrideSlot) {
    remote.clearOverride(slot)
  }
}

class ProfileClient(private val remote: IProfileManager) {
  suspend fun create(type: Profile.Type, name: String, source: String = ""): Uuid {
    return remote.create(type, name, source)
  }

  suspend fun clone(uuid: Uuid): Uuid {
    return remote.clone(uuid)
  }

  suspend fun commit(uuid: Uuid, callback: ((FetchStatus) -> Unit)? = null) {
    remote.commit(
      uuid,
      callback?.let { cb ->
        IFetchObserver { status ->
          cb(json.decodeFromString(status))
        }
      },
    )
  }

  suspend fun release(uuid: Uuid) {
    remote.release(uuid)
  }

  suspend fun delete(uuid: Uuid) {
    remote.delete(uuid)
  }

  suspend fun patch(uuid: Uuid, name: String, source: String, interval: Long) {
    remote.patch(uuid, name, source, interval)
  }

  suspend fun update(uuid: Uuid) {
    remote.update(uuid)
  }

  suspend fun queryByUUID(uuid: Uuid): Profile? {
    return remote.queryByUUID(uuid)?.let { json.decodeFromString(it) }
  }

  suspend fun queryAll(): List<Profile> {
    return json.decodeFromString(remote.queryAll())
  }

  suspend fun queryActive(): Profile? {
    return remote.queryActive()?.let { json.decodeFromString(it) }
  }

  suspend fun setActive(profile: Profile) {
    remote.setActive(profile.uuid)
  }
}

suspend fun <T> withClash(
  context: CoroutineContext = Dispatchers.IO,
  block: suspend ClashClient.() -> T,
): T {
  while (true) {
    val remote = Remote.service.remote.get()
    val client = ClashClient(remote.clash())

    try {
      return withContext(context) { client.block() }
    } catch (_: DeadObjectException) {
      Log.w("Remote services panic")

      Remote.service.remote.reset(remote)
    }
  }
}

suspend fun <T> withProfile(
  context: CoroutineContext = Dispatchers.IO,
  block: suspend ProfileClient.() -> T,
): T {
  while (true) {
    val remote = Remote.service.remote.get()
    val client = ProfileClient(remote.profile())

    try {
      return withContext(context) { client.block() }
    } catch (_: DeadObjectException) {
      Log.w("Remote services panic")

      Remote.service.remote.reset(remote)
    }
  }
}

private val json = Json {
  ignoreUnknownKeys = true
}
