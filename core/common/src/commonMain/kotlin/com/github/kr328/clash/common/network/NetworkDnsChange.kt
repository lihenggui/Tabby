package com.github.kr328.clash.common.network

fun tabbyNetworkObservedDnsChange(
  previousDnsServers: List<String>,
  selectedDnsServers: List<String>,
): List<String>? {
  return selectedDnsServers.takeIf { it.isNotEmpty() && it != previousDnsServers }
}
