package com.github.kr328.clash.service.clash.module

import android.app.Service
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.core.content.getSystemService
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.network.TABBY_NETWORK_SATELLITE_TRANSPORT_MIN_SDK
import com.github.kr328.clash.common.network.TABBY_NETWORK_USB_TRANSPORT_MIN_SDK
import com.github.kr328.clash.common.network.TabbyNetworkTransportState
import com.github.kr328.clash.common.network.tabbyNetworkObservePriority
import com.github.kr328.clash.common.network.tabbyNetworkObservedAvailability
import com.github.kr328.clash.common.network.tabbyNetworkObservedDnsChange
import com.github.kr328.clash.common.network.tabbyNetworkSupportsSatelliteTransport
import com.github.kr328.clash.common.network.tabbyNetworkSupportsUsbTransport
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.service.util.asSocketAddressText
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext

class NetworkObserveModule(service: Service) : Module<Network>(service) {
  private val connectivity = service.getSystemService<ConnectivityManager>()!!
  private val networks: Channel<Network> = Channel(Channel.UNLIMITED)
  private val request =
    NetworkRequest.Builder()
      .apply {
        addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
        addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        addCapability(NetworkCapabilities.NET_CAPABILITY_FOREGROUND)
        addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
      }
      .build()

  private data class NetworkInfo(
    val losingMs: Long = 0,
    val dnsList: List<InetAddress> = emptyList(),
  ) {
    fun isAvailable(currentTimeMillis: Long): Boolean {
      return tabbyNetworkObservedAvailability(
        losingAtMillis = losingMs,
        currentTimeMillis = currentTimeMillis,
      )
    }
  }

  private val networkInfos = ConcurrentHashMap<Network, NetworkInfo>()

  @Volatile private var curDnsList = emptyList<String>()

  private val callback =
    object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        Log.i("NetworkObserve onAvailable network=$network")
        networkInfos[network] = NetworkInfo()
      }

      override fun onLosing(network: Network, maxMsToLive: Int) {
        Log.i("NetworkObserve onLosing network=$network")
        networkInfos.computeIfPresent(network) { _, info ->
          info.copy(losingMs = System.currentTimeMillis() + maxMsToLive)
        }
        notifyDnsChange()

        networks.trySend(network)
      }

      override fun onLost(network: Network) {
        Log.i("NetworkObserve onLost network=$network")
        networkInfos.remove(network)
        notifyDnsChange()

        networks.trySend(network)
      }

      override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
        Log.i("NetworkObserve onLinkPropertiesChanged network=$network $linkProperties")
        networkInfos.computeIfPresent(network) { _, info ->
          info.copy(dnsList = linkProperties.dnsServers)
        }
        notifyDnsChange()

        networks.trySend(network)
      }

      override fun onUnavailable() {
        Log.i("NetworkObserve onUnavailable")
      }
    }

  private fun register(): Boolean {
    Log.i("NetworkObserve start register")
    return try {
      connectivity.registerNetworkCallback(request, callback)

      true
    } catch (e: Exception) {
      Log.w("NetworkObserve register failed", e)

      false
    }
  }

  private fun unregister(): Boolean {
    Log.i("NetworkObserve start unregister")
    try {
      connectivity.unregisterNetworkCallback(callback)
    } catch (e: Exception) {
      Log.w("NetworkObserve unregister failed", e)
    }

    return false
  }

  private fun networkToInt(entry: Map.Entry<Network, NetworkInfo>): Int {
    val capabilities = connectivity.getNetworkCapabilities(entry.key)

    return tabbyNetworkObservePriority(
      transportState = capabilities?.toTabbyTransportState(),
      platformSdk = Build.VERSION.SDK_INT,
      isAvailable = entry.value.isAvailable(System.currentTimeMillis()),
    )
  }

  private fun NetworkCapabilities.toTabbyTransportState(): TabbyNetworkTransportState {
    return TabbyNetworkTransportState(
      hasVpnTransport = hasTransport(NetworkCapabilities.TRANSPORT_VPN),
      hasWifiTransport = hasTransport(NetworkCapabilities.TRANSPORT_WIFI),
      hasEthernetTransport = hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET),
      hasUsbTransport =
        supportsUsbNetworkTransport() && hasTransport(NetworkCapabilities.TRANSPORT_USB),
      hasBluetoothTransport = hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH),
      hasCellularTransport = hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR),
      hasSatelliteTransport =
        supportsSatelliteNetworkTransport() &&
          hasTransport(NetworkCapabilities.TRANSPORT_SATELLITE),
    )
  }

  @ChecksSdkIntAtLeast(api = TABBY_NETWORK_USB_TRANSPORT_MIN_SDK)
  private fun supportsUsbNetworkTransport(): Boolean {
    return tabbyNetworkSupportsUsbTransport(Build.VERSION.SDK_INT)
  }

  @ChecksSdkIntAtLeast(api = TABBY_NETWORK_SATELLITE_TRANSPORT_MIN_SDK)
  private fun supportsSatelliteNetworkTransport(): Boolean {
    return tabbyNetworkSupportsSatelliteTransport(Build.VERSION.SDK_INT)
  }

  private fun notifyDnsChange() {
    val dnsList =
      networkInfos
        .asSequence()
        .minByOrNull { networkToInt(it) }
        ?.value
        ?.dnsList
        .orEmpty()
        .map { x -> x.asSocketAddressText(53) }
    val prevDnsList = curDnsList
    val nextDnsList =
      tabbyNetworkObservedDnsChange(
        previousDnsServers = prevDnsList,
        selectedDnsServers = dnsList,
      )
    if (nextDnsList != null) {
      Log.i("notifyDnsChange $prevDnsList -> $nextDnsList")
      curDnsList = nextDnsList
      Clash.notifyDnsChanged(nextDnsList)
    }
  }

  override suspend fun run() {
    register()

    try {
      while (true) {
        val quit = select {
          networks.onReceive {
            enqueueEvent(it)

            false
          }
        }
        if (quit) {
          return
        }
      }
    } finally {
      withContext(NonCancellable) {
        unregister()

        Log.i("NetworkObserve dns = []")
        Clash.notifyDnsChanged(emptyList())
      }
    }
  }
}
