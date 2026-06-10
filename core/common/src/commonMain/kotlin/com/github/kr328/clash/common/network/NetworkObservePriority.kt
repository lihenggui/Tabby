package com.github.kr328.clash.common.network

const val TABBY_NETWORK_USB_TRANSPORT_MIN_SDK = 31
const val TABBY_NETWORK_SATELLITE_TRANSPORT_MIN_SDK = 35

data class TabbyNetworkTransportState(
  val hasVpnTransport: Boolean = false,
  val hasWifiTransport: Boolean = false,
  val hasEthernetTransport: Boolean = false,
  val hasUsbTransport: Boolean = false,
  val hasBluetoothTransport: Boolean = false,
  val hasCellularTransport: Boolean = false,
  val hasSatelliteTransport: Boolean = false,
)

fun tabbyNetworkObservePriority(
  transportState: TabbyNetworkTransportState?,
  platformSdk: Int,
  isAvailable: Boolean,
): Int {
  val transportPriority =
    when {
      transportState == null -> 100
      transportState.hasVpnTransport -> 90
      transportState.hasWifiTransport -> 0
      transportState.hasEthernetTransport -> 1
      platformSdk >= TABBY_NETWORK_USB_TRANSPORT_MIN_SDK && transportState.hasUsbTransport -> 2
      transportState.hasBluetoothTransport -> 3
      transportState.hasCellularTransport -> 4
      platformSdk >= TABBY_NETWORK_SATELLITE_TRANSPORT_MIN_SDK &&
        transportState.hasSatelliteTransport -> 5
      else -> 20
    }

  return transportPriority + if (isAvailable) 0 else 10
}
