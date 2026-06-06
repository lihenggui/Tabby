package com.github.kr328.clash.settings.ui

import com.github.kr328.clash.core.model.ConfigurationOverride
import kotlin.test.Test
import kotlin.test.assertEquals

class MetaFeatureSettingsStateTest {
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
}
