package com.github.kr328.clash.common.network

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NetworkTransportSdkGateTest {
  @Test
  fun gatesUsbTransportByPlatformSdk() {
    assertFalse(tabbyNetworkSupportsUsbTransport(TABBY_NETWORK_USB_TRANSPORT_MIN_SDK - 1))
    assertTrue(tabbyNetworkSupportsUsbTransport(TABBY_NETWORK_USB_TRANSPORT_MIN_SDK))
  }

  @Test
  fun gatesSatelliteTransportByPlatformSdk() {
    assertFalse(
      tabbyNetworkSupportsSatelliteTransport(TABBY_NETWORK_SATELLITE_TRANSPORT_MIN_SDK - 1)
    )
    assertTrue(tabbyNetworkSupportsSatelliteTransport(TABBY_NETWORK_SATELLITE_TRANSPORT_MIN_SDK))
  }
}
