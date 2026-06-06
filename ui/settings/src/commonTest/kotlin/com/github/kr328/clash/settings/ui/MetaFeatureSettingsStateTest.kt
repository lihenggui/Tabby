package com.github.kr328.clash.settings.ui

import com.github.kr328.clash.core.model.ConfigurationOverride
import kotlin.test.Test
import kotlin.test.assertEquals

class MetaFeatureSettingsStateTest {
  @Test
  fun updatesMetaBooleanBasicOptionsAndPreservesOtherState() {
    val configuration = configurationWithMetaBasicOptions()

    val unifiedDelayUpdated = updateMetaUnifiedDelay(configuration, false)
    val geodataModeUpdated = updateMetaGeodataMode(configuration, null)
    val tcpConcurrentUpdated = updateMetaTcpConcurrent(configuration, true)

    assertEquals(false, unifiedDelayUpdated.unifiedDelay)
    assertEquals(configuration.geodataMode, unifiedDelayUpdated.geodataMode)
    assertEquals(configuration.tcpConcurrent, unifiedDelayUpdated.tcpConcurrent)
    assertEquals(configuration.findProcessMode, unifiedDelayUpdated.findProcessMode)
    assertEquals(configuration.sniffer, unifiedDelayUpdated.sniffer)

    assertEquals(null, geodataModeUpdated.geodataMode)
    assertEquals(configuration.unifiedDelay, geodataModeUpdated.unifiedDelay)
    assertEquals(configuration.tcpConcurrent, geodataModeUpdated.tcpConcurrent)
    assertEquals(configuration.findProcessMode, geodataModeUpdated.findProcessMode)
    assertEquals(configuration.sniffer, geodataModeUpdated.sniffer)

    assertEquals(true, tcpConcurrentUpdated.tcpConcurrent)
    assertEquals(configuration.unifiedDelay, tcpConcurrentUpdated.unifiedDelay)
    assertEquals(configuration.geodataMode, tcpConcurrentUpdated.geodataMode)
    assertEquals(configuration.findProcessMode, tcpConcurrentUpdated.findProcessMode)
    assertEquals(configuration.sniffer, tcpConcurrentUpdated.sniffer)
  }

  @Test
  fun updatesFindProcessModeAndPreservesOtherState() {
    val configuration = configurationWithMetaBasicOptions()

    val strictUpdated =
      updateMetaFindProcessMode(configuration, ConfigurationOverride.FindProcessMode.Strict)
    val cleared = updateMetaFindProcessMode(configuration, null)

    assertEquals(ConfigurationOverride.FindProcessMode.Strict, strictUpdated.findProcessMode)
    assertEquals(configuration.unifiedDelay, strictUpdated.unifiedDelay)
    assertEquals(configuration.geodataMode, strictUpdated.geodataMode)
    assertEquals(configuration.tcpConcurrent, strictUpdated.tcpConcurrent)
    assertEquals(configuration.sniffer, strictUpdated.sniffer)

    assertEquals(null, cleared.findProcessMode)
    assertEquals(configuration.unifiedDelay, cleared.unifiedDelay)
    assertEquals(configuration.geodataMode, cleared.geodataMode)
    assertEquals(configuration.tcpConcurrent, cleared.tcpConcurrent)
    assertEquals(configuration.sniffer, cleared.sniffer)
  }

  @Test
  fun updatesHttpSniffPortsAndPreservesOtherProtocols() {
    val configuration = configurationWithSniffProtocols()

    val updated =
      updateSniffProtocolPorts(
        configuration = configuration,
        protocol = SniffProtocol.Http,
        ports = listOf("80", "8080"),
      )

    assertEquals(listOf("80", "8080"), updated.sniffer.sniff.http.ports)
    assertEquals(configuration.sniffer.sniff.tls, updated.sniffer.sniff.tls)
    assertEquals(configuration.sniffer.sniff.quic, updated.sniffer.sniff.quic)
  }

  @Test
  fun updatesTlsSniffOverrideDestinationAndPreservesOtherProtocols() {
    val configuration = configurationWithSniffProtocols()

    val updated =
      updateSniffProtocolOverrideDestination(
        configuration = configuration,
        protocol = SniffProtocol.Tls,
        overrideDestination = true,
      )

    assertEquals(true, updated.sniffer.sniff.tls.overrideDestination)
    assertEquals(configuration.sniffer.sniff.http, updated.sniffer.sniff.http)
    assertEquals(configuration.sniffer.sniff.quic, updated.sniffer.sniff.quic)
  }

  @Test
  fun clearsQuicSniffPorts() {
    val configuration = configurationWithSniffProtocols()

    val updated =
      updateSniffProtocolPorts(
        configuration = configuration,
        protocol = SniffProtocol.Quic,
        ports = null,
      )

    assertEquals(null, updated.sniffer.sniff.quic.ports)
    assertEquals(configuration.sniffer.sniff.http, updated.sniffer.sniff.http)
    assertEquals(configuration.sniffer.sniff.tls, updated.sniffer.sniff.tls)
  }

  @Test
  fun clearsHttpSniffOverrideDestination() {
    val configuration = configurationWithSniffProtocols()

    val updated =
      updateSniffProtocolOverrideDestination(
        configuration = configuration,
        protocol = SniffProtocol.Http,
        overrideDestination = null,
      )

    assertEquals(null, updated.sniffer.sniff.http.overrideDestination)
    assertEquals(configuration.sniffer.sniff.tls, updated.sniffer.sniff.tls)
    assertEquals(configuration.sniffer.sniff.quic, updated.sniffer.sniff.quic)
  }

  @Test
  fun updatesSnifferEnableAndPreservesSniffProtocols() {
    val configuration = configurationWithSnifferOptions()

    val updated = updateMetaSnifferEnable(configuration, false)

    assertEquals(false, updated.sniffer.enable)
    assertEquals(configuration.sniffer.sniff, updated.sniffer.sniff)
    assertEquals(configuration.sniffer.forceDomain, updated.sniffer.forceDomain)
    assertEquals(configuration.sniffer.skipDomain, updated.sniffer.skipDomain)
  }

  @Test
  fun updatesSnifferBooleanOptionsAndPreservesSniffProtocols() {
    val configuration = configurationWithSnifferOptions()

    val forceDnsMappingUpdated = updateMetaSnifferForceDnsMapping(configuration, null)
    val parsePureIpUpdated = updateMetaSnifferParsePureIp(configuration, true)
    val overrideDestinationUpdated = updateMetaSnifferOverrideDestination(configuration, false)

    assertEquals(null, forceDnsMappingUpdated.sniffer.forceDnsMapping)
    assertEquals(configuration.sniffer.sniff, forceDnsMappingUpdated.sniffer.sniff)
    assertEquals(configuration.sniffer.parsePureIp, forceDnsMappingUpdated.sniffer.parsePureIp)

    assertEquals(true, parsePureIpUpdated.sniffer.parsePureIp)
    assertEquals(configuration.sniffer.sniff, parsePureIpUpdated.sniffer.sniff)
    assertEquals(configuration.sniffer.forceDnsMapping, parsePureIpUpdated.sniffer.forceDnsMapping)

    assertEquals(false, overrideDestinationUpdated.sniffer.overrideDestination)
    assertEquals(configuration.sniffer.sniff, overrideDestinationUpdated.sniffer.sniff)
    assertEquals(
      configuration.sniffer.forceDnsMapping,
      overrideDestinationUpdated.sniffer.forceDnsMapping,
    )
  }

  @Test
  fun updatesSnifferDomainListsAndPreservesSniffProtocols() {
    val configuration = configurationWithSnifferOptions()
    val forceDomain = listOf("+.force.example", "geosite:forced")
    val skipDomain = listOf("+.skip.example")

    val forceDomainUpdated = updateMetaSnifferForceDomain(configuration, forceDomain)
    val skipDomainUpdated = updateMetaSnifferSkipDomain(configuration, skipDomain)

    assertEquals(forceDomain, forceDomainUpdated.sniffer.forceDomain)
    assertEquals(configuration.sniffer.skipDomain, forceDomainUpdated.sniffer.skipDomain)
    assertEquals(configuration.sniffer.sniff, forceDomainUpdated.sniffer.sniff)

    assertEquals(skipDomain, skipDomainUpdated.sniffer.skipDomain)
    assertEquals(configuration.sniffer.forceDomain, skipDomainUpdated.sniffer.forceDomain)
    assertEquals(configuration.sniffer.sniff, skipDomainUpdated.sniffer.sniff)
  }

  @Test
  fun clearsSnifferAddressListsAndPreservesSniffProtocols() {
    val configuration = configurationWithSnifferOptions()

    val skipSrcAddressUpdated = updateMetaSnifferSkipSrcAddress(configuration, null)
    val skipDstAddressUpdated = updateMetaSnifferSkipDstAddress(configuration, null)

    assertEquals(null, skipSrcAddressUpdated.sniffer.skipSrcAddress)
    assertEquals(configuration.sniffer.skipDstAddress, skipSrcAddressUpdated.sniffer.skipDstAddress)
    assertEquals(configuration.sniffer.sniff, skipSrcAddressUpdated.sniffer.sniff)

    assertEquals(null, skipDstAddressUpdated.sniffer.skipDstAddress)
    assertEquals(configuration.sniffer.skipSrcAddress, skipDstAddressUpdated.sniffer.skipSrcAddress)
    assertEquals(configuration.sniffer.sniff, skipDstAddressUpdated.sniffer.sniff)
  }

  private fun configurationWithSniffProtocols(): ConfigurationOverride {
    return ConfigurationOverride(
      sniffer =
        ConfigurationOverride.Sniffer(
          forceDnsMapping = true,
          sniff =
            ConfigurationOverride.Sniff(
              http =
                ConfigurationOverride.ProtocolConfig(
                  ports = listOf("80"),
                  overrideDestination = false,
                ),
              tls =
                ConfigurationOverride.ProtocolConfig(
                  ports = listOf("443"),
                  overrideDestination = false,
                ),
              quic =
                ConfigurationOverride.ProtocolConfig(
                  ports = listOf("443/udp"),
                  overrideDestination = true,
                ),
            ),
        )
    )
  }

  private fun configurationWithMetaBasicOptions(): ConfigurationOverride {
    return ConfigurationOverride(
      unifiedDelay = true,
      geodataMode = false,
      tcpConcurrent = false,
      findProcessMode = ConfigurationOverride.FindProcessMode.Always,
      sniffer = configurationWithSnifferOptions().sniffer,
    )
  }

  private fun configurationWithSnifferOptions(): ConfigurationOverride {
    val sniff = configurationWithSniffProtocols().sniffer.sniff
    return ConfigurationOverride(
      sniffer =
        ConfigurationOverride.Sniffer(
          enable = true,
          sniff = sniff,
          forceDnsMapping = true,
          parsePureIp = false,
          overrideDestination = true,
          forceDomain = listOf("+.force.test"),
          skipDomain = listOf("+.skip.test"),
          skipSrcAddress = listOf("192.0.2.0/24"),
          skipDstAddress = listOf("198.51.100.0/24"),
        )
    )
  }
}
