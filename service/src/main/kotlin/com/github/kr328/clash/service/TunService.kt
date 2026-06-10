package com.github.kr328.clash.service

import android.app.PendingIntent
import android.content.Intent
import android.net.ProxyInfo
import android.net.VpnService
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import com.github.kr328.clash.common.compat.pendingIntentFlags
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.network.TABBY_TUN_ANY_IPV4_ADDRESS
import com.github.kr328.clash.common.network.TABBY_TUN_ANY_IPV6_ADDRESS
import com.github.kr328.clash.common.network.TABBY_TUN_HTTP_PROXY_MIN_SDK
import com.github.kr328.clash.common.network.TABBY_TUN_IPV4_DNS
import com.github.kr328.clash.common.network.TABBY_TUN_IPV4_GATEWAY
import com.github.kr328.clash.common.network.TABBY_TUN_IPV4_SUBNET_PREFIX
import com.github.kr328.clash.common.network.TABBY_TUN_IPV6_DNS
import com.github.kr328.clash.common.network.TABBY_TUN_IPV6_GATEWAY
import com.github.kr328.clash.common.network.TABBY_TUN_IPV6_SUBNET_PREFIX
import com.github.kr328.clash.common.network.TABBY_TUN_METERED_MIN_SDK
import com.github.kr328.clash.common.network.tabbyTunCanSetHttpProxy
import com.github.kr328.clash.common.network.tabbyTunCanSetMetered
import com.github.kr328.clash.common.network.tabbyTunDeviceText
import com.github.kr328.clash.common.network.tabbyTunShouldSetUnderlyingNetworks
import com.github.kr328.clash.common.util.IPNet
import com.github.kr328.clash.common.util.mainIntent
import com.github.kr328.clash.service.clash.clashRuntime
import com.github.kr328.clash.service.clash.module.AppListCacheModule
import com.github.kr328.clash.service.clash.module.CloseModule
import com.github.kr328.clash.service.clash.module.ConfigurationModule
import com.github.kr328.clash.service.clash.module.DynamicNotificationModule
import com.github.kr328.clash.service.clash.module.NetworkObserveModule
import com.github.kr328.clash.service.clash.module.StaticNotificationModule
import com.github.kr328.clash.service.clash.module.SuspendModule
import com.github.kr328.clash.service.clash.module.TimeZoneModule
import com.github.kr328.clash.service.clash.module.TunModule
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.service.util.cancelAndJoinBlocking
import com.github.kr328.clash.service.util.sendClashStarted
import com.github.kr328.clash.service.util.sendClashStopped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext

class TunService : VpnService(), CoroutineScope by CoroutineScope(Dispatchers.Default) {
  private val self: TunService
    get() = this

  private var reason: String? = null

  private val runtime = clashRuntime {
    val store = ServiceStore(self)

    val close = install(CloseModule(self))
    val tun = install(TunModule(self))
    val config = install(ConfigurationModule(self))
    val network = install(NetworkObserveModule(self))

    if (store.dynamicNotification) install(DynamicNotificationModule(self))
    else install(StaticNotificationModule(self))

    install(AppListCacheModule(self))
    install(TimeZoneModule(self))
    install(SuspendModule(self))

    try {
      tun.open()

      while (isActive) {
        val quit = select {
          close.onEvent { true }
          config.onEvent {
            reason = it.message

            true
          }
          network.onEvent { n ->
            if (tabbyTunShouldSetUnderlyingNetworks(Build.VERSION.SDK_INT)) {
              setUnderlyingNetworks(arrayOf(n))
            }

            false
          }
        }

        if (quit) break
      }
    } catch (e: Exception) {
      Log.e("Create clash runtime: ${e.message}", e)

      reason = e.message
    } finally {
      withContext(NonCancellable) {
        tun.close()

        stopSelf()
      }
    }
  }

  override fun onCreate() {
    super.onCreate()

    if (StatusProvider.serviceRunning) return stopSelf()

    StatusProvider.serviceRunning = true

    StaticNotificationModule.createNotificationChannel(this)
    StaticNotificationModule.notifyLoadingNotification(this)

    runtime.launch()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    sendClashStarted()

    return super.onStartCommand(intent, flags, startId)
  }

  override fun onDestroy() {
    TunModule.requestStop()

    StatusProvider.serviceRunning = false

    sendClashStopped(reason)

    cancelAndJoinBlocking()

    Log.i("TunService destroyed: ${reason ?: "successfully"}")

    super.onDestroy()
  }

  override fun onTrimMemory(level: Int) {
    super.onTrimMemory(level)

    runtime.requestGc()
  }

  private fun TunModule.open() {
    val store = ServiceStore(self)

    val device =
      with(Builder()) {
        // Interface address
        addAddress(TABBY_TUN_IPV4_GATEWAY, TABBY_TUN_IPV4_SUBNET_PREFIX)
        if (store.allowIpv6) {
          addAddress(TABBY_TUN_IPV6_GATEWAY, TABBY_TUN_IPV6_SUBNET_PREFIX)
        }

        // Route
        if (store.bypassPrivateNetwork) {
          BYPASS_PRIVATE_ROUTE_V4.forEach { addRoute(it.ip, it.prefix) }
          if (store.allowIpv6) {
            BYPASS_PRIVATE_ROUTE_V6.forEach { addRoute(it.ip, it.prefix) }
          }

          // Route of virtual DNS
          addRoute(TABBY_TUN_IPV4_DNS, 32)
          if (store.allowIpv6) {
            addRoute(TABBY_TUN_IPV6_DNS, 128)
          }
        } else {
          addRoute(TABBY_TUN_ANY_IPV4_ADDRESS, 0)
          if (store.allowIpv6) {
            addRoute(TABBY_TUN_ANY_IPV6_ADDRESS, 0)
          }
        }

        // Access Control
        when (store.accessControlMode) {
          AcceptAll -> Unit
          AcceptSelected -> {
            (store.accessControlPackages + packageName).forEach {
              runCatching { addAllowedApplication(it) }
                .onFailure { e -> Log.e("Add allowed application $it failed: ${e.message}", e) }
            }
          }
          DenySelected -> {
            (store.accessControlPackages - packageName).forEach {
              runCatching { addDisallowedApplication(it) }
                .onFailure { e -> Log.e("Add disallowed application $it failed: ${e.message}", e) }
            }
          }
        }

        // Blocking
        setBlocking(false)

        // Mtu
        setMtu(TUN_MTU)

        // Session Name
        setSession("Clash")

        // Virtual Dns Server
        addDnsServer(TABBY_TUN_IPV4_DNS)
        if (store.allowIpv6) {
          addDnsServer(TABBY_TUN_IPV6_DNS)
        }

        // Open MainActivity
        setConfigureIntent(
          PendingIntent.getActivity(
            self,
            R.id.nf_vpn_status,
            service.mainIntent {
              addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            pendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT),
          )
        )

        // Metered
        if (canSetTunMetered()) {
          setMetered(false)
        }

        // System Proxy
        if (canSetTunHttpProxy(store.systemProxy)) {
          listenHttp()?.let {
            setHttpProxy(
              ProxyInfo.buildDirectProxy(
                it.address.hostAddress,
                it.port,
                HTTP_PROXY_BLACK_LIST +
                  if (store.bypassPrivateNetwork) HTTP_PROXY_LOCAL_LIST else emptyList(),
              )
            )
          }
        }

        if (store.allowBypass) {
          allowBypass()
        }

        val tunDeviceText =
          tabbyTunDeviceText(
            allowIpv6 = store.allowIpv6,
            dnsHijacking = store.dnsHijacking,
          )

        TunModule.TunDevice(
          fd =
            establish()?.detachFd()
              ?: throw NullPointerException("Establish VPN rejected by system"),
          stack = store.tunStackMode,
          gateway = tunDeviceText.gateway,
          portal = tunDeviceText.portal,
          dns = tunDeviceText.dns,
        )
      }

    attach(device)
  }

  @ChecksSdkIntAtLeast(api = TABBY_TUN_METERED_MIN_SDK)
  private fun canSetTunMetered(): Boolean {
    return tabbyTunCanSetMetered(Build.VERSION.SDK_INT)
  }

  @ChecksSdkIntAtLeast(api = TABBY_TUN_HTTP_PROXY_MIN_SDK)
  private fun canSetTunHttpProxy(systemProxy: Boolean): Boolean {
    return tabbyTunCanSetHttpProxy(
      platformSdk = Build.VERSION.SDK_INT,
      systemProxy = systemProxy,
    )
  }

  companion object {
    private const val TUN_MTU = 9000
    /** Public IPv4 route set used when bypassing private and other special-use ranges. */
    private val BYPASS_PRIVATE_ROUTE_V4: List<IPNet> =
      listOf(
          "1.0.0.0/8",
          "2.0.0.0/7",
          "4.0.0.0/6",
          "8.0.0.0/7",
          "11.0.0.0/8",
          "12.0.0.0/6",
          "16.0.0.0/4",
          "32.0.0.0/3",
          "64.0.0.0/3",
          "96.0.0.0/4",
          "112.0.0.0/5",
          "120.0.0.0/6",
          "124.0.0.0/7",
          "126.0.0.0/8",
          "128.0.0.0/3",
          "160.0.0.0/5",
          "168.0.0.0/8",
          "169.0.0.0/9",
          "169.128.0.0/10",
          "169.192.0.0/11",
          "169.224.0.0/12",
          "169.240.0.0/13",
          "169.248.0.0/14",
          "169.252.0.0/15",
          "169.255.0.0/16",
          "170.0.0.0/7",
          "172.0.0.0/12",
          "172.32.0.0/11",
          "172.64.0.0/10",
          "172.128.0.0/9",
          "173.0.0.0/8",
          "174.0.0.0/7",
          "176.0.0.0/4",
          "192.0.0.0/9",
          "192.128.0.0/11",
          "192.160.0.0/13",
          "192.169.0.0/16",
          "192.170.0.0/15",
          "192.172.0.0/14",
          "192.176.0.0/12",
          "192.192.0.0/10",
          "193.0.0.0/8",
          "194.0.0.0/7",
          "196.0.0.0/6",
          "200.0.0.0/5",
          "208.0.0.0/4",
          "240.0.0.0/5",
          "248.0.0.0/6",
          "252.0.0.0/7",
          "254.0.0.0/8",
          "255.0.0.0/9",
          "255.128.0.0/10",
          "255.192.0.0/11",
          "255.224.0.0/12",
          "255.240.0.0/13",
          "255.248.0.0/14",
          "255.252.0.0/15",
          "255.254.0.0/16",
          "255.255.0.0/17",
          "255.255.128.0/18",
          "255.255.192.0/19",
          "255.255.224.0/20",
          "255.255.240.0/21",
          "255.255.248.0/22",
          "255.255.252.0/23",
          "255.255.254.0/24",
          "255.255.255.0/25",
          "255.255.255.128/26",
          "255.255.255.192/27",
          "255.255.255.224/28",
          "255.255.255.240/29",
          "255.255.255.248/30",
          "255.255.255.252/31",
          "255.255.255.254/32",
        )
        .map(IPNet::parse)
    /** Exclude fc00::/7, fe80::/10, ff00::/8 */
    private val BYPASS_PRIVATE_ROUTE_V6: List<IPNet> =
      listOf(
          "::/1",
          "8000::/2",
          "c000::/3",
          "e000::/4",
          "f000::/5",
          "f800::/6",
          "fe00::/9",
          "fec0::/10",
        )
        .map(IPNet::parse)
    private val HTTP_PROXY_LOCAL_LIST: List<String> =
      listOf(
        "localhost",
        "*.local",
        "127.*",
        "10.*",
        "172.16.*",
        "172.17.*",
        "172.18.*",
        "172.19.*",
        "172.2*",
        "172.30.*",
        "172.31.*",
        "192.168.*",
      )
    private val HTTP_PROXY_BLACK_LIST: List<String> =
      listOf("*zhihu.com", "*zhimg.com", "*jd.com", "100ime-iat-api.xfyun.cn", "*360buyimg.com")
  }
}
