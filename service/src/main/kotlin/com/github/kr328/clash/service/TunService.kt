package com.github.kr328.clash.service

import android.app.PendingIntent
import android.content.Intent
import android.net.ProxyInfo
import android.net.VpnService
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import com.github.kr328.clash.common.compat.pendingIntentFlags
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.network.TABBY_TUN_HTTP_PROXY_MIN_SDK
import com.github.kr328.clash.common.network.TABBY_TUN_METERED_MIN_SDK
import com.github.kr328.clash.common.network.tabbyTunCanSetHttpProxy
import com.github.kr328.clash.common.network.tabbyTunCanSetMetered
import com.github.kr328.clash.common.network.tabbyTunDeviceText
import com.github.kr328.clash.common.network.tabbyTunHttpProxyExclusionList
import com.github.kr328.clash.common.network.tabbyTunNetworkPlan
import com.github.kr328.clash.common.network.tabbyTunShouldSetUnderlyingNetworks
import com.github.kr328.clash.common.service.TabbyServiceCreateAction
import com.github.kr328.clash.common.service.tabbyServiceCreateAction
import com.github.kr328.clash.common.util.mainIntent
import com.github.kr328.clash.core.model.accessControlPackagePlan
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

    when (tabbyServiceCreateAction(StatusProvider.serviceRunning)) {
      TabbyServiceCreateAction.StopDuplicate -> return stopSelf()
      TabbyServiceCreateAction.StartService -> Unit
    }

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
        val networkPlan =
          tabbyTunNetworkPlan(
            allowIpv6 = store.allowIpv6,
            bypassPrivateNetwork = store.bypassPrivateNetwork,
          )

        networkPlan.addresses.forEach { addAddress(it.ip, it.prefix) }
        networkPlan.routes.forEach { addRoute(it.ip, it.prefix) }

        val accessControlPlan =
          accessControlPackagePlan(
            mode = store.accessControlMode,
            selectedPackages = store.accessControlPackages,
            ownPackageName = packageName,
          )

        accessControlPlan.allowedPackages.forEach {
          runCatching { addAllowedApplication(it) }
            .onFailure { e -> Log.e("Add allowed application $it failed: ${e.message}", e) }
        }
        accessControlPlan.disallowedPackages.forEach {
          runCatching { addDisallowedApplication(it) }
            .onFailure { e -> Log.e("Add disallowed application $it failed: ${e.message}", e) }
        }

        // Blocking
        setBlocking(false)

        // Mtu
        setMtu(TUN_MTU)

        // Session Name
        setSession("Clash")

        networkPlan.dnsServers.forEach { addDnsServer(it) }

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
                tabbyTunHttpProxyExclusionList(bypassPrivateNetwork = store.bypassPrivateNetwork),
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
  }
}
