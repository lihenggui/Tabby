package com.github.kr328.clash.settings.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.engine.android.AndroidEngineController
import com.github.kr328.clash.engine.api.EngineController
import com.github.kr328.clash.settings.ui.OverridePersistAction
import com.github.kr328.clash.settings.ui.OverrideSettingsActions
import com.github.kr328.clash.settings.ui.overridePersistAction
import com.github.kr328.clash.settings.ui.updateOverrideAllowLan
import com.github.kr328.clash.settings.ui.updateOverrideAllowOrigins
import com.github.kr328.clash.settings.ui.updateOverrideAllowPrivateNetwork
import com.github.kr328.clash.settings.ui.updateOverrideAppendSystemDns
import com.github.kr328.clash.settings.ui.updateOverrideAuthentication
import com.github.kr328.clash.settings.ui.updateOverrideBindAddress
import com.github.kr328.clash.settings.ui.updateOverrideDnsDefaultServer
import com.github.kr328.clash.settings.ui.updateOverrideDnsEnable
import com.github.kr328.clash.settings.ui.updateOverrideDnsEnhancedMode
import com.github.kr328.clash.settings.ui.updateOverrideDnsFakeIpFilter
import com.github.kr328.clash.settings.ui.updateOverrideDnsFakeIpFilterMode
import com.github.kr328.clash.settings.ui.updateOverrideDnsFallback
import com.github.kr328.clash.settings.ui.updateOverrideDnsFallbackDomain
import com.github.kr328.clash.settings.ui.updateOverrideDnsFallbackGeoIp
import com.github.kr328.clash.settings.ui.updateOverrideDnsFallbackGeoIpCode
import com.github.kr328.clash.settings.ui.updateOverrideDnsFallbackIpcidr
import com.github.kr328.clash.settings.ui.updateOverrideDnsIpv6
import com.github.kr328.clash.settings.ui.updateOverrideDnsListen
import com.github.kr328.clash.settings.ui.updateOverrideDnsNameServer
import com.github.kr328.clash.settings.ui.updateOverrideDnsNameserverPolicy
import com.github.kr328.clash.settings.ui.updateOverrideDnsPreferH3
import com.github.kr328.clash.settings.ui.updateOverrideDnsUseHosts
import com.github.kr328.clash.settings.ui.updateOverrideExternalController
import com.github.kr328.clash.settings.ui.updateOverrideExternalControllerTls
import com.github.kr328.clash.settings.ui.updateOverrideHosts
import com.github.kr328.clash.settings.ui.updateOverrideHttpPort
import com.github.kr328.clash.settings.ui.updateOverrideIpv6
import com.github.kr328.clash.settings.ui.updateOverrideLogLevel
import com.github.kr328.clash.settings.ui.updateOverrideMixedPort
import com.github.kr328.clash.settings.ui.updateOverrideMode
import com.github.kr328.clash.settings.ui.updateOverrideRedirectPort
import com.github.kr328.clash.settings.ui.updateOverrideSecret
import com.github.kr328.clash.settings.ui.updateOverrideSocksPort
import com.github.kr328.clash.settings.ui.updateOverrideTproxyPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class OverrideSettingsViewModel(app: Application) :
  AndroidViewModel(app), OverrideSettingsActions, DefaultLifecycleObserver {
  private val engineController: EngineController = AndroidEngineController(app)
  @Volatile private var skipPersist = false

  val configuration: StateFlow<ConfigurationOverride>
    field = MutableStateFlow(ConfigurationOverride())

  init {
    viewModelScope.launch {
      configuration.value = engineController.queryPersistOverride()
    }
  }

  override fun onStop(owner: LifecycleOwner) {
    // Intended to use non-viewModel scope as we need the action to be called on disposed.
    Global.launch {
      when (val action = overridePersistAction(skipPersist, configuration.value)) {
        OverridePersistAction.Clear -> engineController.clearPersistOverride()
        is OverridePersistAction.Patch ->
          engineController.patchPersistOverride(action.configuration)
      }
    }
  }

  fun resetOverride() {
    skipPersist = true
  }

  override fun updateHttpPort(value: Int?) = configuration.update {
    updateOverrideHttpPort(it, value)
  }

  override fun updateSocksPort(value: Int?) = configuration.update {
    updateOverrideSocksPort(it, value)
  }

  override fun updateRedirectPort(value: Int?) = configuration.update {
    updateOverrideRedirectPort(it, value)
  }

  override fun updateTproxyPort(value: Int?) = configuration.update {
    updateOverrideTproxyPort(it, value)
  }

  override fun updateMixedPort(value: Int?) = configuration.update {
    updateOverrideMixedPort(it, value)
  }

  override fun updateAuthentication(value: List<String>?) = configuration.update {
    updateOverrideAuthentication(it, value)
  }

  override fun updateAllowLan(value: Boolean?) = configuration.update {
    updateOverrideAllowLan(it, value)
  }

  override fun updateIpv6(value: Boolean?) = configuration.update { updateOverrideIpv6(it, value) }

  override fun updateBindAddress(value: String?) = configuration.update {
    updateOverrideBindAddress(it, value)
  }

  override fun updateExternalController(value: String?) = configuration.update {
    updateOverrideExternalController(it, value)
  }

  override fun updateExternalControllerTls(value: String?) = configuration.update {
    updateOverrideExternalControllerTls(it, value)
  }

  override fun updateAllowOrigins(value: List<String>?) = configuration.update {
    updateOverrideAllowOrigins(it, value)
  }

  override fun updateAllowPrivateNetwork(value: Boolean?) = configuration.update {
    updateOverrideAllowPrivateNetwork(it, value)
  }

  override fun updateSecret(value: String?) = configuration.update {
    updateOverrideSecret(it, value)
  }

  override fun updateMode(value: TunnelState.Mode?) = configuration.update {
    updateOverrideMode(it, value)
  }

  override fun updateLogLevel(value: LogMessage.Level?) = configuration.update {
    updateOverrideLogLevel(it, value)
  }

  override fun updateHosts(value: Map<String, String>?) = configuration.update {
    updateOverrideHosts(it, value)
  }

  override fun updateDnsEnable(value: Boolean?) = configuration.update {
    updateOverrideDnsEnable(it, value)
  }

  override fun updateDnsPreferH3(value: Boolean?) = configuration.update {
    updateOverrideDnsPreferH3(it, value)
  }

  override fun updateDnsListen(value: String?) = configuration.update {
    updateOverrideDnsListen(it, value)
  }

  override fun updateAppendSystemDns(value: Boolean?) = configuration.update {
    updateOverrideAppendSystemDns(it, value)
  }

  override fun updateDnsIpv6(value: Boolean?) = configuration.update {
    updateOverrideDnsIpv6(it, value)
  }

  override fun updateDnsUseHosts(value: Boolean?) = configuration.update {
    updateOverrideDnsUseHosts(it, value)
  }

  override fun updateDnsEnhancedMode(value: ConfigurationOverride.DnsEnhancedMode?) =
    configuration.update {
      updateOverrideDnsEnhancedMode(it, value)
    }

  override fun updateDnsNameServer(value: List<String>?) = configuration.update {
    updateOverrideDnsNameServer(it, value)
  }

  override fun updateDnsFallback(value: List<String>?) = configuration.update {
    updateOverrideDnsFallback(it, value)
  }

  override fun updateDnsDefaultServer(value: List<String>?) = configuration.update {
    updateOverrideDnsDefaultServer(it, value)
  }

  override fun updateDnsFakeIpFilter(value: List<String>?) = configuration.update {
    updateOverrideDnsFakeIpFilter(it, value)
  }

  override fun updateDnsFakeIpFilterMode(value: ConfigurationOverride.FilterMode?) =
    configuration.update {
      updateOverrideDnsFakeIpFilterMode(it, value)
    }

  override fun updateDnsGeoIpFallback(value: Boolean?) = configuration.update {
    updateOverrideDnsFallbackGeoIp(it, value)
  }

  override fun updateDnsGeoIpCode(value: String?) = configuration.update {
    updateOverrideDnsFallbackGeoIpCode(it, value)
  }

  override fun updateDnsDomainFallback(value: List<String>?) = configuration.update {
    updateOverrideDnsFallbackDomain(it, value)
  }

  override fun updateDnsIpcidrFallback(value: List<String>?) = configuration.update {
    updateOverrideDnsFallbackIpcidr(it, value)
  }

  override fun updateDnsNameserverPolicy(value: Map<String, String>?) = configuration.update {
    updateOverrideDnsNameserverPolicy(it, value)
  }
}
