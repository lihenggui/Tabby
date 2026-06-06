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

internal fun updateMetaSnifferEnable(
  configuration: ConfigurationOverride,
  enable: Boolean?,
): ConfigurationOverride {
  return configuration.updateSniffer { it.copy(enable = enable) }
}

internal fun updateMetaSnifferForceDnsMapping(
  configuration: ConfigurationOverride,
  forceDnsMapping: Boolean?,
): ConfigurationOverride {
  return configuration.updateSniffer { it.copy(forceDnsMapping = forceDnsMapping) }
}

internal fun updateMetaSnifferParsePureIp(
  configuration: ConfigurationOverride,
  parsePureIp: Boolean?,
): ConfigurationOverride {
  return configuration.updateSniffer { it.copy(parsePureIp = parsePureIp) }
}

internal fun updateMetaSnifferOverrideDestination(
  configuration: ConfigurationOverride,
  overrideDestination: Boolean?,
): ConfigurationOverride {
  return configuration.updateSniffer { it.copy(overrideDestination = overrideDestination) }
}

internal fun updateMetaSnifferForceDomain(
  configuration: ConfigurationOverride,
  forceDomain: List<String>?,
): ConfigurationOverride {
  return configuration.updateSniffer { it.copy(forceDomain = forceDomain) }
}

internal fun updateMetaSnifferSkipDomain(
  configuration: ConfigurationOverride,
  skipDomain: List<String>?,
): ConfigurationOverride {
  return configuration.updateSniffer { it.copy(skipDomain = skipDomain) }
}

internal fun updateMetaSnifferSkipSrcAddress(
  configuration: ConfigurationOverride,
  skipSrcAddress: List<String>?,
): ConfigurationOverride {
  return configuration.updateSniffer { it.copy(skipSrcAddress = skipSrcAddress) }
}

internal fun updateMetaSnifferSkipDstAddress(
  configuration: ConfigurationOverride,
  skipDstAddress: List<String>?,
): ConfigurationOverride {
  return configuration.updateSniffer { it.copy(skipDstAddress = skipDstAddress) }
}

private fun ConfigurationOverride.updateSniffer(
  transform: (ConfigurationOverride.Sniffer) -> ConfigurationOverride.Sniffer
): ConfigurationOverride {
  return copy(sniffer = transform(sniffer))
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
