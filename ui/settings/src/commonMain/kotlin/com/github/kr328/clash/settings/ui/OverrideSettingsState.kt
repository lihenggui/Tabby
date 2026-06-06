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
