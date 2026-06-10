package com.github.kr328.clash.common.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NetworkDnsChangeTest {
  @Test
  fun ignoresEmptySelectedDnsServers() {
    assertNull(
      tabbyNetworkObservedDnsChange(
        previousDnsServers = listOf("1.1.1.1:53"),
        selectedDnsServers = emptyList(),
      )
    )
  }

  @Test
  fun ignoresUnchangedSelectedDnsServers() {
    assertNull(
      tabbyNetworkObservedDnsChange(
        previousDnsServers = listOf("1.1.1.1:53", "8.8.8.8:53"),
        selectedDnsServers = listOf("1.1.1.1:53", "8.8.8.8:53"),
      )
    )
  }

  @Test
  fun returnsSelectedDnsServersWhenTheyChange() {
    assertEquals(
      listOf("8.8.8.8:53", "9.9.9.9:53"),
      tabbyNetworkObservedDnsChange(
        previousDnsServers = listOf("1.1.1.1:53"),
        selectedDnsServers = listOf("8.8.8.8:53", "9.9.9.9:53"),
      ),
    )
  }
}
