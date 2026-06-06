package com.github.kr328.clash.settings.vm

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import com.github.kr328.clash.core.model.AccessControlMode
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.store.UiStore
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.settings.ui.NetworkSettingsUiState
import com.github.kr328.clash.settings.ui.networkSettingsHasSystemProxyOptionFromPlatformSdk
import com.github.kr328.clash.settings.ui.networkSettingsInitialUiState
import com.github.kr328.clash.settings.ui.updateNetworkSettingsAccessControlMode
import com.github.kr328.clash.settings.ui.updateNetworkSettingsAllowBypass
import com.github.kr328.clash.settings.ui.updateNetworkSettingsAllowIpv6
import com.github.kr328.clash.settings.ui.updateNetworkSettingsBypassPrivateNetwork
import com.github.kr328.clash.settings.ui.updateNetworkSettingsDnsHijacking
import com.github.kr328.clash.settings.ui.updateNetworkSettingsEnableVpn
import com.github.kr328.clash.settings.ui.updateNetworkSettingsSystemProxy
import com.github.kr328.clash.settings.ui.updateNetworkSettingsTunStackMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

internal class NetworkSettingsViewModel(app: Application) : AndroidViewModel(app) {
  private val uiStore = UiStore(app)
  private val serviceStore = ServiceStore(app)

  val clashRunning: StateFlow<Boolean> = Remote.broadcasts.clashRunningFlow

  val uiState: StateFlow<NetworkSettingsUiState>
    field =
      MutableStateFlow(
        networkSettingsInitialUiState(
          hasSystemProxyOption =
            networkSettingsHasSystemProxyOptionFromPlatformSdk(
              sdkVersion = Build.VERSION.SDK_INT,
              systemProxySdkVersion = Build.VERSION_CODES.Q,
            ),
          enableVpn = uiStore.enableVpn,
          bypassPrivateNetwork = serviceStore.bypassPrivateNetwork,
          dnsHijacking = serviceStore.dnsHijacking,
          allowBypass = serviceStore.allowBypass,
          allowIpv6 = serviceStore.allowIpv6,
          systemProxy = serviceStore.systemProxy,
          tunStackMode = serviceStore.tunStackMode,
          accessControlMode = serviceStore.accessControlMode,
        )
      )

  fun updateEnableVpn(value: Boolean) {
    uiStore.enableVpn = value
    uiState.update { updateNetworkSettingsEnableVpn(it, value) }
  }

  fun updateBypassPrivateNetwork(value: Boolean) {
    serviceStore.bypassPrivateNetwork = value
    uiState.update { updateNetworkSettingsBypassPrivateNetwork(it, value) }
  }

  fun updateDnsHijacking(value: Boolean) {
    serviceStore.dnsHijacking = value
    uiState.update { updateNetworkSettingsDnsHijacking(it, value) }
  }

  fun updateAllowBypass(value: Boolean) {
    serviceStore.allowBypass = value
    uiState.update { updateNetworkSettingsAllowBypass(it, value) }
  }

  fun updateAllowIpv6(value: Boolean) {
    serviceStore.allowIpv6 = value
    uiState.update { updateNetworkSettingsAllowIpv6(it, value) }
  }

  fun updateSystemProxy(value: Boolean) {
    serviceStore.systemProxy = value
    uiState.update { updateNetworkSettingsSystemProxy(it, value) }
  }

  fun updateTunStackMode(value: String) {
    serviceStore.tunStackMode = value
    uiState.update { updateNetworkSettingsTunStackMode(it, value) }
  }

  fun updateAccessControlMode(value: AccessControlMode) {
    serviceStore.accessControlMode = value
    uiState.update { updateNetworkSettingsAccessControlMode(it, value) }
  }
}
