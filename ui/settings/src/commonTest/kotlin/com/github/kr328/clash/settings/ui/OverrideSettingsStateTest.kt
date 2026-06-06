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

  @Test
  fun updatesDnsFallbackGeoIpAndPreservesOtherFallbackFields() {
    val configuration = configurationWithDnsFallbackFilter()

    val updated = updateOverrideDnsFallbackGeoIp(configuration, false)

    assertEquals(false, updated.dns.fallbackFilter.geoIp)
    assertEquals(configuration.dns.fallbackFilter.geoIpCode, updated.dns.fallbackFilter.geoIpCode)
    assertEquals(configuration.dns.fallbackFilter.domain, updated.dns.fallbackFilter.domain)
    assertEquals(configuration.dns.fallbackFilter.ipcidr, updated.dns.fallbackFilter.ipcidr)
    assertEquals(configuration.dns.nameServer, updated.dns.nameServer)
  }

  @Test
  fun clearsDnsFallbackGeoIpCodeAndPreservesOtherFallbackFields() {
    val configuration = configurationWithDnsFallbackFilter()

    val updated = updateOverrideDnsFallbackGeoIpCode(configuration, null)

    assertEquals(null, updated.dns.fallbackFilter.geoIpCode)
    assertEquals(configuration.dns.fallbackFilter.geoIp, updated.dns.fallbackFilter.geoIp)
    assertEquals(configuration.dns.fallbackFilter.domain, updated.dns.fallbackFilter.domain)
    assertEquals(configuration.dns.fallbackFilter.ipcidr, updated.dns.fallbackFilter.ipcidr)
    assertEquals(configuration.dns.nameServer, updated.dns.nameServer)
  }

  @Test
  fun updatesDnsFallbackDomainAndPreservesOtherFallbackFields() {
    val configuration = configurationWithDnsFallbackFilter()
    val domain = listOf("geosite:private", "+.internal.example")

    val updated = updateOverrideDnsFallbackDomain(configuration, domain)

    assertEquals(domain, updated.dns.fallbackFilter.domain)
    assertEquals(configuration.dns.fallbackFilter.geoIp, updated.dns.fallbackFilter.geoIp)
    assertEquals(configuration.dns.fallbackFilter.geoIpCode, updated.dns.fallbackFilter.geoIpCode)
    assertEquals(configuration.dns.fallbackFilter.ipcidr, updated.dns.fallbackFilter.ipcidr)
    assertEquals(configuration.dns.nameServer, updated.dns.nameServer)
  }

  @Test
  fun clearsDnsFallbackIpcidrAndPreservesOtherFallbackFields() {
    val configuration = configurationWithDnsFallbackFilter()

    val updated = updateOverrideDnsFallbackIpcidr(configuration, null)

    assertEquals(null, updated.dns.fallbackFilter.ipcidr)
    assertEquals(configuration.dns.fallbackFilter.geoIp, updated.dns.fallbackFilter.geoIp)
    assertEquals(configuration.dns.fallbackFilter.geoIpCode, updated.dns.fallbackFilter.geoIpCode)
    assertEquals(configuration.dns.fallbackFilter.domain, updated.dns.fallbackFilter.domain)
    assertEquals(configuration.dns.nameServer, updated.dns.nameServer)
  }

  private fun configurationWithDnsFallbackFilter(): ConfigurationOverride {
    return ConfigurationOverride(
      dns =
        ConfigurationOverride.Dns(
          nameServer = listOf("https://dns.example/dns-query"),
          fallbackFilter =
            ConfigurationOverride.DnsFallbackFilter(
              geoIp = true,
              geoIpCode = "CN",
              domain = listOf("geosite:example"),
              ipcidr = listOf("192.0.2.0/24"),
            ),
        )
    )
  }
}
