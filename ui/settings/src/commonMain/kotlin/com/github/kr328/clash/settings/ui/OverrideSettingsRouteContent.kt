package com.github.kr328.clash.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.core.model.TunnelState

@Composable
fun OverrideSettingsRouteContent(
  onResetCompleted: () -> Unit,
  modifier: Modifier = Modifier,
  initialConfiguration: ConfigurationOverride = overrideSettingsInitialConfiguration(),
  onConfigurationChange: (ConfigurationOverride) -> Unit = {},
  onReset: () -> Unit = {},
) {
  val configurationState = remember { mutableStateOf(initialConfiguration) }
  val currentOnConfigurationChange = rememberUpdatedState(onConfigurationChange)
  val currentOnReset = rememberUpdatedState(onReset)

  fun updateConfiguration(transform: (ConfigurationOverride) -> ConfigurationOverride) {
    val updated = transform(configurationState.value)
    configurationState.value = updated
    currentOnConfigurationChange.value(updated)
  }

  val actions = remember {
    object : OverrideSettingsActions {
      override fun updateHttpPort(value: Int?) = updateConfiguration {
        updateOverrideHttpPort(it, value)
      }

      override fun updateSocksPort(value: Int?) = updateConfiguration {
        updateOverrideSocksPort(it, value)
      }

      override fun updateRedirectPort(value: Int?) = updateConfiguration {
        updateOverrideRedirectPort(it, value)
      }

      override fun updateTproxyPort(value: Int?) = updateConfiguration {
        updateOverrideTproxyPort(it, value)
      }

      override fun updateMixedPort(value: Int?) = updateConfiguration {
        updateOverrideMixedPort(it, value)
      }

      override fun updateAuthentication(value: List<String>?) = updateConfiguration {
        updateOverrideAuthentication(it, value)
      }

      override fun updateAllowLan(value: Boolean?) = updateConfiguration {
        updateOverrideAllowLan(it, value)
      }

      override fun updateIpv6(value: Boolean?) = updateConfiguration {
        updateOverrideIpv6(it, value)
      }

      override fun updateBindAddress(value: String?) = updateConfiguration {
        updateOverrideBindAddress(it, value)
      }

      override fun updateExternalController(value: String?) = updateConfiguration {
        updateOverrideExternalController(it, value)
      }

      override fun updateExternalControllerTls(value: String?) = updateConfiguration {
        updateOverrideExternalControllerTls(it, value)
      }

      override fun updateAllowOrigins(value: List<String>?) = updateConfiguration {
        updateOverrideAllowOrigins(it, value)
      }

      override fun updateAllowPrivateNetwork(value: Boolean?) = updateConfiguration {
        updateOverrideAllowPrivateNetwork(it, value)
      }

      override fun updateSecret(value: String?) = updateConfiguration {
        updateOverrideSecret(it, value)
      }

      override fun updateMode(value: TunnelState.Mode?) = updateConfiguration {
        updateOverrideMode(it, value)
      }

      override fun updateLogLevel(value: LogMessage.Level?) = updateConfiguration {
        updateOverrideLogLevel(it, value)
      }

      override fun updateHosts(value: Map<String, String>?) = updateConfiguration {
        updateOverrideHosts(it, value)
      }

      override fun updateDnsEnable(value: Boolean?) = updateConfiguration {
        updateOverrideDnsEnable(it, value)
      }

      override fun updateDnsPreferH3(value: Boolean?) = updateConfiguration {
        updateOverrideDnsPreferH3(it, value)
      }

      override fun updateDnsListen(value: String?) = updateConfiguration {
        updateOverrideDnsListen(it, value)
      }

      override fun updateAppendSystemDns(value: Boolean?) = updateConfiguration {
        updateOverrideAppendSystemDns(it, value)
      }

      override fun updateDnsIpv6(value: Boolean?) = updateConfiguration {
        updateOverrideDnsIpv6(it, value)
      }

      override fun updateDnsUseHosts(value: Boolean?) = updateConfiguration {
        updateOverrideDnsUseHosts(it, value)
      }

      override fun updateDnsEnhancedMode(value: ConfigurationOverride.DnsEnhancedMode?) =
        updateConfiguration {
          updateOverrideDnsEnhancedMode(it, value)
        }

      override fun updateDnsNameServer(value: List<String>?) = updateConfiguration {
        updateOverrideDnsNameServer(it, value)
      }

      override fun updateDnsFallback(value: List<String>?) = updateConfiguration {
        updateOverrideDnsFallback(it, value)
      }

      override fun updateDnsDefaultServer(value: List<String>?) = updateConfiguration {
        updateOverrideDnsDefaultServer(it, value)
      }

      override fun updateDnsFakeIpFilter(value: List<String>?) = updateConfiguration {
        updateOverrideDnsFakeIpFilter(it, value)
      }

      override fun updateDnsFakeIpFilterMode(value: ConfigurationOverride.FilterMode?) =
        updateConfiguration {
          updateOverrideDnsFakeIpFilterMode(it, value)
        }

      override fun updateDnsGeoIpFallback(value: Boolean?) = updateConfiguration {
        updateOverrideDnsFallbackGeoIp(it, value)
      }

      override fun updateDnsGeoIpCode(value: String?) = updateConfiguration {
        updateOverrideDnsFallbackGeoIpCode(it, value)
      }

      override fun updateDnsDomainFallback(value: List<String>?) = updateConfiguration {
        updateOverrideDnsFallbackDomain(it, value)
      }

      override fun updateDnsIpcidrFallback(value: List<String>?) = updateConfiguration {
        updateOverrideDnsFallbackIpcidr(it, value)
      }

      override fun updateDnsNameserverPolicy(value: Map<String, String>?) = updateConfiguration {
        updateOverrideDnsNameserverPolicy(it, value)
      }
    }
  }

  LaunchedEffect(initialConfiguration) {
    if (configurationState.value != initialConfiguration) {
      configurationState.value = initialConfiguration
    }
  }

  OverrideSettingsNavigatorContent { onOpenEditableTextMap, onOpenEditableTextList ->
    val showResetConfirmDialog = remember { mutableStateOf(false) }

    OverrideSettingsContent(
      configuration = configurationState.value,
      actions = actions,
      modifier = modifier,
      showResetConfirmDialog = showResetConfirmDialog.value,
      onShowResetConfirmDialogChange = { showResetConfirmDialog.value = it },
      onResetConfirmed = {
        val resetConfiguration = overrideSettingsInitialConfiguration()
        configurationState.value = resetConfiguration
        currentOnConfigurationChange.value(resetConfiguration)
        currentOnReset.value()
        onResetCompleted()
      },
      onOpenEditableTextMap = onOpenEditableTextMap,
      onOpenEditableTextList = onOpenEditableTextList,
    )
  }
}
