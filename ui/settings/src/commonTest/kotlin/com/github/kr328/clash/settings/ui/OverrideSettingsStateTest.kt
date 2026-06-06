package com.github.kr328.clash.settings.ui

import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.core.model.TunnelState
import kotlin.test.Test
import kotlin.test.assertEquals

class OverrideSettingsStateTest {
  @Test
  fun updatesPortOverridesAndPreservesNestedState() {
    val configuration = configurationWithGeneralOverrides()

    val httpUpdated = updateOverrideHttpPort(configuration, 7890)
    val socksUpdated = updateOverrideSocksPort(configuration, 7891)
    val redirectUpdated = updateOverrideRedirectPort(configuration, null)
    val tproxyUpdated = updateOverrideTproxyPort(configuration, 7893)
    val mixedUpdated = updateOverrideMixedPort(configuration, null)

    assertEquals(7890, httpUpdated.httpPort)
    assertEquals(configuration.socksPort, httpUpdated.socksPort)
    assertEquals(configuration.dns, httpUpdated.dns)
    assertEquals(configuration.sniffer, httpUpdated.sniffer)

    assertEquals(7891, socksUpdated.socksPort)
    assertEquals(configuration.httpPort, socksUpdated.httpPort)
    assertEquals(configuration.dns, socksUpdated.dns)
    assertEquals(configuration.sniffer, socksUpdated.sniffer)

    assertEquals(null, redirectUpdated.redirectPort)
    assertEquals(configuration.dns, redirectUpdated.dns)
    assertEquals(configuration.sniffer, redirectUpdated.sniffer)

    assertEquals(7893, tproxyUpdated.tproxyPort)
    assertEquals(configuration.dns, tproxyUpdated.dns)
    assertEquals(configuration.sniffer, tproxyUpdated.sniffer)

    assertEquals(null, mixedUpdated.mixedPort)
    assertEquals(configuration.dns, mixedUpdated.dns)
    assertEquals(configuration.sniffer, mixedUpdated.sniffer)
  }

  @Test
  fun updatesGeneralOverridesAndPreservesNestedState() {
    val configuration = configurationWithGeneralOverrides()
    val authentication = listOf("user:pass", "token:secret")

    val authenticationUpdated = updateOverrideAuthentication(configuration, authentication)
    val allowLanUpdated = updateOverrideAllowLan(configuration, null)
    val ipv6Updated = updateOverrideIpv6(configuration, true)
    val secretUpdated = updateOverrideSecret(configuration, null)
    val modeUpdated = updateOverrideMode(configuration, TunnelState.Mode.Global)
    val logLevelUpdated = updateOverrideLogLevel(configuration, LogMessage.Level.Warning)

    assertEquals(authentication, authenticationUpdated.authentication)
    assertEquals(configuration.dns, authenticationUpdated.dns)
    assertEquals(configuration.sniffer, authenticationUpdated.sniffer)

    assertEquals(null, allowLanUpdated.allowLan)
    assertEquals(configuration.dns, allowLanUpdated.dns)
    assertEquals(configuration.sniffer, allowLanUpdated.sniffer)

    assertEquals(true, ipv6Updated.ipv6)
    assertEquals(configuration.dns, ipv6Updated.dns)
    assertEquals(configuration.sniffer, ipv6Updated.sniffer)

    assertEquals(null, secretUpdated.secret)
    assertEquals(configuration.dns, secretUpdated.dns)
    assertEquals(configuration.sniffer, secretUpdated.sniffer)

    assertEquals(TunnelState.Mode.Global, modeUpdated.mode)
    assertEquals(configuration.dns, modeUpdated.dns)
    assertEquals(configuration.sniffer, modeUpdated.sniffer)

    assertEquals(LogMessage.Level.Warning, logLevelUpdated.logLevel)
    assertEquals(configuration.dns, logLevelUpdated.dns)
    assertEquals(configuration.sniffer, logLevelUpdated.sniffer)
  }

  @Test
  fun updatesExternalControllerOverridesAndPreservesCorsFields() {
    val configuration = configurationWithGeneralOverrides()
    val allowOrigins = listOf("https://tabby.example", "https://internal.example")

    val bindAddressUpdated = updateOverrideBindAddress(configuration, "127.0.0.1")
    val controllerUpdated = updateOverrideExternalController(configuration, "127.0.0.1:9090")
    val controllerTlsUpdated = updateOverrideExternalControllerTls(configuration, null)
    val allowOriginsUpdated = updateOverrideAllowOrigins(configuration, allowOrigins)
    val allowPrivateNetworkUpdated = updateOverrideAllowPrivateNetwork(configuration, null)

    assertEquals("127.0.0.1", bindAddressUpdated.bindAddress)
    assertEquals(configuration.externalControllerCors, bindAddressUpdated.externalControllerCors)
    assertEquals(configuration.dns, bindAddressUpdated.dns)

    assertEquals("127.0.0.1:9090", controllerUpdated.externalController)
    assertEquals(configuration.externalControllerCors, controllerUpdated.externalControllerCors)
    assertEquals(configuration.dns, controllerUpdated.dns)

    assertEquals(null, controllerTlsUpdated.externalControllerTLS)
    assertEquals(configuration.externalControllerCors, controllerTlsUpdated.externalControllerCors)
    assertEquals(configuration.dns, controllerTlsUpdated.dns)

    assertEquals(allowOrigins, allowOriginsUpdated.externalControllerCors.allowOrigins)
    assertEquals(
      configuration.externalControllerCors.allowPrivateNetwork,
      allowOriginsUpdated.externalControllerCors.allowPrivateNetwork,
    )
    assertEquals(configuration.dns, allowOriginsUpdated.dns)

    assertEquals(null, allowPrivateNetworkUpdated.externalControllerCors.allowPrivateNetwork)
    assertEquals(
      configuration.externalControllerCors.allowOrigins,
      allowPrivateNetworkUpdated.externalControllerCors.allowOrigins,
    )
    assertEquals(configuration.dns, allowPrivateNetworkUpdated.dns)
  }

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

  private fun configurationWithGeneralOverrides(): ConfigurationOverride {
    return ConfigurationOverride(
      httpPort = 8080,
      socksPort = 8081,
      redirectPort = 8082,
      tproxyPort = 8083,
      mixedPort = 8084,
      authentication = listOf("old:credential"),
      allowLan = true,
      bindAddress = "0.0.0.0",
      mode = TunnelState.Mode.Rule,
      logLevel = LogMessage.Level.Info,
      ipv6 = false,
      externalController = "0.0.0.0:9090",
      externalControllerTLS = "0.0.0.0:9443",
      externalControllerCors =
        ConfigurationOverride.ExternalControllerCors(
          allowOrigins = listOf("https://old.example"),
          allowPrivateNetwork = true,
        ),
      secret = "secret",
      dns =
        ConfigurationOverride.Dns(
          enable = true,
          nameServer = listOf("https://dns.example/dns-query"),
        ),
      app = ConfigurationOverride.App(appendSystemDns = true),
      sniffer =
        ConfigurationOverride.Sniffer(
          enable = true,
          forceDomain = listOf("+.force.example"),
        ),
    )
  }
}
