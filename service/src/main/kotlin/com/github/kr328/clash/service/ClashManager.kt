package com.github.kr328.clash.service

import android.content.Context
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.service.data.Selection
import com.github.kr328.clash.service.data.SelectionDao
import com.github.kr328.clash.service.remote.IClashManager
import com.github.kr328.clash.service.remote.ILogObserver
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.service.util.sendOverrideChanged
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ClashManager(private val context: Context) :
  IClashManager, CoroutineScope by CoroutineScope(Dispatchers.IO) {
  private val store = ServiceStore(context)
  private var logReceiver: ReceiveChannel<LogMessage>? = null

  override fun queryTunnelState(): String {
    return json.encodeToString(Clash.queryTunnelState())
  }

  override fun queryTrafficTotal(): Long {
    return Clash.queryTrafficTotal().packed
  }

  override fun queryProxyGroupNames(excludeNotSelectable: Boolean): List<String> {
    return Clash.queryGroupNames(excludeNotSelectable)
  }

  override fun queryProxyGroup(name: String, proxySort: ProxySort): String {
    return json.encodeToString(Clash.queryGroup(name, proxySort))
  }

  override fun queryConfiguration(): String {
    return json.encodeToString(Clash.queryConfiguration())
  }

  override fun queryProviders(): String {
    return json.encodeToString(Clash.queryProviders())
  }

  override fun queryOverride(slot: Clash.OverrideSlot): String {
    return json.encodeToString(Clash.queryOverride(slot))
  }

  override fun patchSelector(group: String, name: String): Boolean {
    return Clash.patchSelector(group, name).also {
      val current = store.activeProfile ?: return@also

      Global.launch {
        try {
          if (it) {
            SelectionDao().setSelected(Selection(current, group, name))
          } else {
            SelectionDao().removeSelected(current, group)
          }
        } catch (e: Exception) {
          Log.w("Persist selector failed", e)
        }
      }
    }
  }

  override fun patchOverride(slot: Clash.OverrideSlot, configuration: String) {
    Clash.patchOverride(slot, json.decodeFromString<ConfigurationOverride>(configuration))

    context.sendOverrideChanged()
  }

  override fun clearOverride(slot: Clash.OverrideSlot) {
    Clash.clearOverride(slot)
  }

  override suspend fun healthCheck(group: String) {
    return Clash.healthCheck(group).await()
  }

  override suspend fun healthCheckProxy(group: String, name: String) {
    return Clash.healthCheckProxy(group, name).await()
  }

  override suspend fun updateProvider(type: Provider.Type, name: String) {
    return Clash.updateProvider(type, name).await()
  }

  override fun setLogObserver(observer: ILogObserver?) {
    synchronized(this) {
      logReceiver?.apply {
        cancel()

        Clash.forceGc()
      }

      if (observer != null) {
        logReceiver =
          Clash.subscribeLogcat().also { c ->
            launch {
              try {
                while (isActive) {
                  observer.newItem(json.encodeToString(c.receive()))
                }
              } catch (e: CancellationException) {
                // intended behavior
                // ignore
              } catch (e: Exception) {
                Log.w("UI crashed", e)
              } finally {
                withContext(NonCancellable) {
                  c.cancel()

                  Clash.forceGc()
                }
              }
            }
          }
      }
    }
  }
}

private val json = Json {
  ignoreUnknownKeys = true
}
