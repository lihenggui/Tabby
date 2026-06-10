package com.github.kr328.clash.engine.android

import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.service.remote.IClashManager
import com.github.kr328.clash.service.remote.ILogObserver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class AndroidLogRepositoryTest {
  @Test
  fun decodesServiceLogPayload() {
    val log =
      decodeAndroidLogMessage(
        """{"level":"warning","message":"proxy changed","time":1234,"ignored":true}"""
      )

    assertEquals(LogMessage(LogMessage.Level.Warning, "proxy changed", 1234), log)
  }

  @Test
  @OptIn(ExperimentalCoroutinesApi::class)
  fun observeAndroidLogsRegistersAndClearsObserver() = runTest {
    val clash = RecordingClashManager()

    val item = async { observeAndroidLogs(clash).first() }
    runCurrent()

    assertNotNull(clash.observer)
      .newItem("""{"level":"info","message":"connected","time":4321,"ignored":true}""")

    assertEquals(LogMessage(LogMessage.Level.Info, "connected", 4321), item.await())
    runCurrent()

    assertNull(clash.observer)
  }
}

private class RecordingClashManager : IClashManager {
  var observer: ILogObserver? = null
    private set

  override fun setLogObserver(observer: ILogObserver?) {
    this.observer = observer
  }

  override fun queryTunnelState(): String = unsupported()

  override fun queryTrafficTotal(): Long = unsupported()

  override fun queryProxyGroupNames(excludeNotSelectable: Boolean): List<String> = unsupported()

  override fun queryProxyGroup(name: String, proxySort: ProxySort): String = unsupported()

  override fun queryConfiguration(): String = unsupported()

  override fun queryProviders(): String = unsupported()

  override fun patchSelector(group: String, name: String): Boolean = unsupported()

  override suspend fun healthCheck(group: String) {
    unsupported()
  }

  override suspend fun healthCheckProxy(group: String, name: String) {
    unsupported()
  }

  override suspend fun updateProvider(type: Provider.Type, name: String) {
    unsupported()
  }

  override fun queryOverride(slot: Clash.OverrideSlot): String = unsupported()

  override fun patchOverride(slot: Clash.OverrideSlot, configuration: String) {
    unsupported()
  }

  override fun clearOverride(slot: Clash.OverrideSlot) {
    unsupported()
  }

  private fun unsupported(): Nothing = error("Unexpected IClashManager call")
}
