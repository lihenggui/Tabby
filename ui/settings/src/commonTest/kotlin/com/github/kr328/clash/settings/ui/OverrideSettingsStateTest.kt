package com.github.kr328.clash.settings.ui

import com.github.kr328.clash.core.model.ConfigurationOverride
import kotlin.test.Test
import kotlin.test.assertEquals

class OverrideSettingsStateTest {
  @Test
  fun updatesHostsAndIncrementsRevision() {
    val configuration = ConfigurationOverride(revision = 7)
    val hosts = linkedMapOf("example.com" to "127.0.0.1", "internal.test" to "192.0.2.10")

    val updated = updateOverrideHosts(configuration, hosts)

    assertEquals(hosts, updated.hosts)
    assertEquals(8, updated.revision)
  }

  @Test
  fun clearsHostsAndStillIncrementsRevision() {
    val configuration =
      ConfigurationOverride(
        hosts = mapOf("example.com" to "127.0.0.1"),
        revision = 2,
      )

    val updated = updateOverrideHosts(configuration, null)

    assertEquals(null, updated.hosts)
    assertEquals(3, updated.revision)
  }

  @Test
  fun updatesDnsNameserverPolicyAndIncrementsRevision() {
    val configuration = ConfigurationOverride(revision = 11)
    val policy = linkedMapOf("geosite:example" to "https://dns.example/dns-query")

    val updated = updateOverrideDnsNameserverPolicy(configuration, policy)

    assertEquals(policy, updated.dns.nameserverPolicy)
    assertEquals(12, updated.revision)
  }

  @Test
  fun clearsDnsNameserverPolicyAndStillIncrementsRevision() {
    val configuration =
      ConfigurationOverride(
        dns =
          ConfigurationOverride.Dns(
            nameserverPolicy = mapOf("geosite:example" to "https://dns.example/dns-query")
          ),
        revision = 5,
      )

    val updated = updateOverrideDnsNameserverPolicy(configuration, null)

    assertEquals(null, updated.dns.nameserverPolicy)
    assertEquals(6, updated.revision)
  }
}
