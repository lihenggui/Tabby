package com.github.kr328.clash.settingsstore

import com.github.kr328.clash.core.model.AccessControlMode
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyNetworkSettingsRepositoryTest {
  @Test
  fun queryUsesDefaultsWhenKeysAreAbsent() {
    val repository = testRepository()
    val defaults =
      TabbyNetworkSettings(
        hasSystemProxyOption = false,
        enableVpn = false,
        bypassPrivateNetwork = false,
        dnsHijacking = false,
        allowBypass = false,
        allowIpv6 = true,
        systemProxy = false,
        tunStackMode = "mixed",
        accessControlMode = AccessControlMode.DenySelected,
      )

    assertEquals(defaults, repository.query(defaults))
  }

  @Test
  fun settersPersistValuesOverDefaults() {
    val repository = testRepository()

    repository.setEnableVpn(false)
    repository.setBypassPrivateNetwork(false)
    repository.setDnsHijacking(false)
    repository.setAllowBypass(false)
    repository.setAllowIpv6(true)
    repository.setSystemProxy(false)
    repository.setTunStackMode("gvisor")
    repository.setAccessControlMode(AccessControlMode.AcceptSelected)

    assertEquals(
      TabbyNetworkSettings(
        hasSystemProxyOption = false,
        enableVpn = false,
        bypassPrivateNetwork = false,
        dnsHijacking = false,
        allowBypass = false,
        allowIpv6 = true,
        systemProxy = false,
        tunStackMode = "gvisor",
        accessControlMode = AccessControlMode.AcceptSelected,
      ),
      repository.query(defaults = TabbyNetworkSettings(hasSystemProxyOption = false)),
    )
  }

  private fun testRepository(): TabbyNetworkSettingsRepository {
    return TabbyNetworkSettingsRepository(
      uiStoreProvider = MapSettings().asStoreProvider(),
      serviceStoreProvider = MapSettings().asStoreProvider(),
    )
  }
}
