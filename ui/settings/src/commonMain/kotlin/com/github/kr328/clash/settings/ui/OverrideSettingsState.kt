package com.github.kr328.clash.settings.ui

import com.github.kr328.clash.core.model.ConfigurationOverride

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

private fun ConfigurationOverride.updateDnsFallbackFilter(
  transform: (ConfigurationOverride.DnsFallbackFilter) -> ConfigurationOverride.DnsFallbackFilter
): ConfigurationOverride {
  return copy(dns = dns.copy(fallbackFilter = transform(dns.fallbackFilter)))
}
