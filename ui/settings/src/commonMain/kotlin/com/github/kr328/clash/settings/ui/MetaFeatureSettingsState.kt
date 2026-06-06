package com.github.kr328.clash.settings.ui

import com.github.kr328.clash.core.model.ConfigurationOverride

internal enum class SniffProtocol {
  Http,
  Tls,
  Quic,
}

internal fun updateSniffProtocolPorts(
  configuration: ConfigurationOverride,
  protocol: SniffProtocol,
  ports: List<String>?,
): ConfigurationOverride {
  return configuration.copy(
    sniffer =
      configuration.sniffer.copy(
        sniff = configuration.sniffer.sniff.updateProtocol(protocol) { it.copy(ports = ports) }
      )
  )
}

internal fun updateSniffProtocolOverrideDestination(
  configuration: ConfigurationOverride,
  protocol: SniffProtocol,
  overrideDestination: Boolean?,
): ConfigurationOverride {
  return configuration.copy(
    sniffer =
      configuration.sniffer.copy(
        sniff =
          configuration.sniffer.sniff.updateProtocol(protocol) {
            it.copy(overrideDestination = overrideDestination)
          }
      )
  )
}

private fun ConfigurationOverride.Sniff.updateProtocol(
  protocol: SniffProtocol,
  transform: (ConfigurationOverride.ProtocolConfig) -> ConfigurationOverride.ProtocolConfig,
): ConfigurationOverride.Sniff {
  return when (protocol) {
    SniffProtocol.Http -> copy(http = transform(http))
    SniffProtocol.Tls -> copy(tls = transform(tls))
    SniffProtocol.Quic -> copy(quic = transform(quic))
  }
}
