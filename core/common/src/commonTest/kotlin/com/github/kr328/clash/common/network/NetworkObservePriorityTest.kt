package com.github.kr328.clash.common.network

import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkObservePriorityTest {
  @Test
  fun prefersGeneralInternetTransportsInDefaultRouteOrder() {
    assertEquals(
      0,
      tabbyNetworkObservePriority(
        transportState = TabbyNetworkTransportState(hasWifiTransport = true),
        platformSdk = TABBY_NETWORK_USB_TRANSPORT_MIN_SDK,
        isAvailable = true,
      ),
    )
    assertEquals(
      1,
      tabbyNetworkObservePriority(
        transportState = TabbyNetworkTransportState(hasEthernetTransport = true),
        platformSdk = TABBY_NETWORK_USB_TRANSPORT_MIN_SDK,
        isAvailable = true,
      ),
    )
    assertEquals(
      3,
      tabbyNetworkObservePriority(
        transportState = TabbyNetworkTransportState(hasBluetoothTransport = true),
        platformSdk = TABBY_NETWORK_USB_TRANSPORT_MIN_SDK,
        isAvailable = true,
      ),
    )
    assertEquals(
      4,
      tabbyNetworkObservePriority(
        transportState = TabbyNetworkTransportState(hasCellularTransport = true),
        platformSdk = TABBY_NETWORK_USB_TRANSPORT_MIN_SDK,
        isAvailable = true,
      ),
    )
  }

  @Test
  fun penalizesUnavailableNetworksAfterTransportPriority() {
    assertEquals(
      10,
      tabbyNetworkObservePriority(
        transportState = TabbyNetworkTransportState(hasWifiTransport = true),
        platformSdk = TABBY_NETWORK_USB_TRANSPORT_MIN_SDK,
        isAvailable = false,
      ),
    )
  }

  @Test
  fun mapsLosingDeadlineToAvailability() {
    assertEquals(
      true,
      tabbyNetworkObservedAvailability(
        losingAtMillis = 900,
        currentTimeMillis = 1000,
      ),
    )
    assertEquals(
      false,
      tabbyNetworkObservedAvailability(
        losingAtMillis = 1000,
        currentTimeMillis = 1000,
      ),
    )
    assertEquals(
      false,
      tabbyNetworkObservedAvailability(
        losingAtMillis = 1100,
        currentTimeMillis = 1000,
      ),
    )
  }

  @Test
  fun treatsMissingCapabilitiesAsLowestPriority() {
    assertEquals(
      100,
      tabbyNetworkObservePriority(
        transportState = null,
        platformSdk = TABBY_NETWORK_USB_TRANSPORT_MIN_SDK,
        isAvailable = true,
      ),
    )
  }

  @Test
  fun keepsVpnBelowGeneralInternetTransports() {
    assertEquals(
      90,
      tabbyNetworkObservePriority(
        transportState =
          TabbyNetworkTransportState(hasVpnTransport = true, hasWifiTransport = true),
        platformSdk = TABBY_NETWORK_USB_TRANSPORT_MIN_SDK,
        isAvailable = true,
      ),
    )
  }

  @Test
  fun gatesUsbTransportByPlatformSdk() {
    assertEquals(
      20,
      tabbyNetworkObservePriority(
        transportState = TabbyNetworkTransportState(hasUsbTransport = true),
        platformSdk = TABBY_NETWORK_USB_TRANSPORT_MIN_SDK - 1,
        isAvailable = true,
      ),
    )
    assertEquals(
      2,
      tabbyNetworkObservePriority(
        transportState = TabbyNetworkTransportState(hasUsbTransport = true),
        platformSdk = TABBY_NETWORK_USB_TRANSPORT_MIN_SDK,
        isAvailable = true,
      ),
    )
  }

  @Test
  fun gatesSatelliteTransportByPlatformSdk() {
    assertEquals(
      20,
      tabbyNetworkObservePriority(
        transportState = TabbyNetworkTransportState(hasSatelliteTransport = true),
        platformSdk = TABBY_NETWORK_SATELLITE_TRANSPORT_MIN_SDK - 1,
        isAvailable = true,
      ),
    )
    assertEquals(
      5,
      tabbyNetworkObservePriority(
        transportState = TabbyNetworkTransportState(hasSatelliteTransport = true),
        platformSdk = TABBY_NETWORK_SATELLITE_TRANSPORT_MIN_SDK,
        isAvailable = true,
      ),
    )
  }
}
