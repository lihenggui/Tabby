package com.github.kr328.clash.settings.ui

import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.core.model.TunnelState

internal sealed interface OverridePersistAction {
  data object Clear : OverridePersistAction

  data class Patch(val configuration: ConfigurationOverride) : OverridePersistAction
}

internal fun overrideSettingsInitialConfiguration(): ConfigurationOverride {
  return ConfigurationOverride()
}

internal fun overridePersistAction(
  skipPersist: Boolean,
  configuration: ConfigurationOverride,
): OverridePersistAction {
  return if (skipPersist) {
    OverridePersistAction.Clear
  } else {
    OverridePersistAction.Patch(configuration)
  }
}

internal fun updateOverrideHttpPort(
  configuration: ConfigurationOverride,
  httpPort: Int?,
): ConfigurationOverride {
  return configuration.copy(httpPort = httpPort)
}

internal fun updateOverrideSocksPort(
  configuration: ConfigurationOverride,
  socksPort: Int?,
): ConfigurationOverride {
  return configuration.copy(socksPort = socksPort)
}

internal fun updateOverrideRedirectPort(
  configuration: ConfigurationOverride,
  redirectPort: Int?,
): ConfigurationOverride {
  return configuration.copy(redirectPort = redirectPort)
}

internal fun updateOverrideTproxyPort(
  configuration: ConfigurationOverride,
  tproxyPort: Int?,
): ConfigurationOverride {
  return configuration.copy(tproxyPort = tproxyPort)
}

internal fun updateOverrideMixedPort(
  configuration: ConfigurationOverride,
  mixedPort: Int?,
): ConfigurationOverride {
  return configuration.copy(mixedPort = mixedPort)
}

internal fun updateOverrideAuthentication(
  configuration: ConfigurationOverride,
  authentication: List<String>?,
): ConfigurationOverride {
  return configuration.copy(authentication = authentication)
}

internal fun updateOverrideAllowLan(
  configuration: ConfigurationOverride,
  allowLan: Boolean?,
): ConfigurationOverride {
  return configuration.copy(allowLan = allowLan)
}

internal fun updateOverrideIpv6(
  configuration: ConfigurationOverride,
  ipv6: Boolean?,
): ConfigurationOverride {
  return configuration.copy(ipv6 = ipv6)
}

internal fun updateOverrideBindAddress(
  configuration: ConfigurationOverride,
  bindAddress: String?,
): ConfigurationOverride {
  return configuration.copy(bindAddress = bindAddress)
}

internal fun updateOverrideExternalController(
  configuration: ConfigurationOverride,
  externalController: String?,
): ConfigurationOverride {
  return configuration.copy(externalController = externalController)
}

internal fun updateOverrideExternalControllerTls(
  configuration: ConfigurationOverride,
  externalControllerTLS: String?,
): ConfigurationOverride {
  return configuration.copy(externalControllerTLS = externalControllerTLS)
}

internal fun updateOverrideAllowOrigins(
  configuration: ConfigurationOverride,
  allowOrigins: List<String>?,
): ConfigurationOverride {
  return configuration.updateExternalControllerCors { it.copy(allowOrigins = allowOrigins) }
}

internal fun updateOverrideAllowPrivateNetwork(
  configuration: ConfigurationOverride,
  allowPrivateNetwork: Boolean?,
): ConfigurationOverride {
  return configuration.updateExternalControllerCors {
    it.copy(allowPrivateNetwork = allowPrivateNetwork)
  }
}

internal fun updateOverrideSecret(
  configuration: ConfigurationOverride,
  secret: String?,
): ConfigurationOverride {
  return configuration.copy(secret = secret)
}

internal fun updateOverrideMode(
  configuration: ConfigurationOverride,
  mode: TunnelState.Mode?,
): ConfigurationOverride {
  return configuration.copy(mode = mode)
}

internal fun updateOverrideLogLevel(
  configuration: ConfigurationOverride,
  logLevel: LogMessage.Level?,
): ConfigurationOverride {
  return configuration.copy(logLevel = logLevel)
}

internal fun updateOverrideDnsEnable(
  configuration: ConfigurationOverride,
  enable: Boolean?,
): ConfigurationOverride {
  return configuration.updateDns { it.copy(enable = enable) }
}

internal fun updateOverrideDnsPreferH3(
  configuration: ConfigurationOverride,
  preferH3: Boolean?,
): ConfigurationOverride {
  return configuration.updateDns { it.copy(preferH3 = preferH3) }
}

internal fun updateOverrideDnsListen(
  configuration: ConfigurationOverride,
  listen: String?,
): ConfigurationOverride {
  return configuration.updateDns { it.copy(listen = listen) }
}

internal fun updateOverrideAppendSystemDns(
  configuration: ConfigurationOverride,
  appendSystemDns: Boolean?,
): ConfigurationOverride {
  return configuration.copy(app = configuration.app.copy(appendSystemDns = appendSystemDns))
}

internal fun updateOverrideDnsIpv6(
  configuration: ConfigurationOverride,
  ipv6: Boolean?,
): ConfigurationOverride {
  return configuration.updateDns { it.copy(ipv6 = ipv6) }
}

internal fun updateOverrideDnsUseHosts(
  configuration: ConfigurationOverride,
  useHosts: Boolean?,
): ConfigurationOverride {
  return configuration.updateDns { it.copy(useHosts = useHosts) }
}

internal fun updateOverrideDnsEnhancedMode(
  configuration: ConfigurationOverride,
  enhancedMode: ConfigurationOverride.DnsEnhancedMode?,
): ConfigurationOverride {
  return configuration.updateDns { it.copy(enhancedMode = enhancedMode) }
}

internal fun updateOverrideDnsNameServer(
  configuration: ConfigurationOverride,
  nameServer: List<String>?,
): ConfigurationOverride {
  return configuration.updateDns { it.copy(nameServer = nameServer) }
}

internal fun updateOverrideDnsFallback(
  configuration: ConfigurationOverride,
  fallback: List<String>?,
): ConfigurationOverride {
  return configuration.updateDns { it.copy(fallback = fallback) }
}

internal fun updateOverrideDnsDefaultServer(
  configuration: ConfigurationOverride,
  defaultServer: List<String>?,
): ConfigurationOverride {
  return configuration.updateDns { it.copy(defaultServer = defaultServer) }
}

internal fun updateOverrideDnsFakeIpFilter(
  configuration: ConfigurationOverride,
  fakeIpFilter: List<String>?,
): ConfigurationOverride {
  return configuration.updateDns { it.copy(fakeIpFilter = fakeIpFilter) }
}

internal fun updateOverrideDnsFakeIpFilterMode(
  configuration: ConfigurationOverride,
  fakeIPFilterMode: ConfigurationOverride.FilterMode?,
): ConfigurationOverride {
  return configuration.updateDns { it.copy(fakeIPFilterMode = fakeIPFilterMode) }
}

internal fun updateOverrideHosts(
  configuration: ConfigurationOverride,
  hosts: Map<String, String>?,
): ConfigurationOverride {
  return configuration.copy(hosts = hosts, revision = configuration.revision + 1)
}

internal fun updateOverrideDnsNameserverPolicy(
  configuration: ConfigurationOverride,
  nameserverPolicy: Map<String, String>?,
): ConfigurationOverride {
  return configuration.copy(
    dns = configuration.dns.copy(nameserverPolicy = nameserverPolicy),
    revision = configuration.revision + 1,
  )
}

internal fun updateOverrideDnsFallbackGeoIp(
  configuration: ConfigurationOverride,
  geoIp: Boolean?,
): ConfigurationOverride {
  return configuration.updateDnsFallbackFilter { it.copy(geoIp = geoIp) }
}

internal fun updateOverrideDnsFallbackGeoIpCode(
  configuration: ConfigurationOverride,
  geoIpCode: String?,
): ConfigurationOverride {
  return configuration.updateDnsFallbackFilter { it.copy(geoIpCode = geoIpCode) }
}

internal fun updateOverrideDnsFallbackDomain(
  configuration: ConfigurationOverride,
  domain: List<String>?,
): ConfigurationOverride {
  return configuration.updateDnsFallbackFilter { it.copy(domain = domain) }
}

internal fun updateOverrideDnsFallbackIpcidr(
  configuration: ConfigurationOverride,
  ipcidr: List<String>?,
): ConfigurationOverride {
  return configuration.updateDnsFallbackFilter { it.copy(ipcidr = ipcidr) }
}

private fun ConfigurationOverride.updateExternalControllerCors(
  transform:
    (ConfigurationOverride.ExternalControllerCors) -> ConfigurationOverride.ExternalControllerCors
): ConfigurationOverride {
  return copy(externalControllerCors = transform(externalControllerCors))
}

private fun ConfigurationOverride.updateDns(
  transform: (ConfigurationOverride.Dns) -> ConfigurationOverride.Dns
): ConfigurationOverride {
  return copy(dns = transform(dns))
}

private fun ConfigurationOverride.updateDnsFallbackFilter(
  transform: (ConfigurationOverride.DnsFallbackFilter) -> ConfigurationOverride.DnsFallbackFilter
): ConfigurationOverride {
  return updateDns { it.copy(fallbackFilter = transform(it.fallbackFilter)) }
}
