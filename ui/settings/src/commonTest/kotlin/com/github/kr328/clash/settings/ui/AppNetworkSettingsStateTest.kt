package com.github.kr328.clash.settings.ui

import com.github.kr328.clash.core.model.AccessControlMode
import com.github.kr328.clash.core.model.DarkMode
import kotlin.test.Test
import kotlin.test.assertEquals

class AppNetworkSettingsStateTest {
  @Test
  fun createsInitialAppSettingsState() {
    assertEquals(
      appSettingsUiState(),
      appSettingsInitialUiState(
        autoRestart = true,
        darkMode = DarkMode.Auto,
        hideAppIcon = false,
        hideFromRecents = false,
        dynamicNotification = true,
      ),
    )
  }

  @Test
  fun updatesAppSettingsFieldsAndPreservesOtherValues() {
    val uiState = appSettingsUiState()

    val autoRestartUpdated = updateAppSettingsAutoRestart(uiState, false)
    val darkModeUpdated = updateAppSettingsDarkMode(uiState, DarkMode.ForceLight)
    val hideAppIconUpdated = updateAppSettingsHideAppIcon(uiState, true)
    val hideFromRecentsUpdated = updateAppSettingsHideFromRecents(uiState, true)
    val dynamicNotificationUpdated = updateAppSettingsDynamicNotification(uiState, false)

    assertEquals(false, autoRestartUpdated.autoRestart)
    assertEquals(uiState.darkMode, autoRestartUpdated.darkMode)
    assertEquals(uiState.hideAppIcon, autoRestartUpdated.hideAppIcon)
    assertEquals(uiState.hideFromRecents, autoRestartUpdated.hideFromRecents)
    assertEquals(uiState.dynamicNotification, autoRestartUpdated.dynamicNotification)

    assertEquals(DarkMode.ForceLight, darkModeUpdated.darkMode)
    assertEquals(uiState.autoRestart, darkModeUpdated.autoRestart)
    assertEquals(uiState.hideAppIcon, darkModeUpdated.hideAppIcon)
    assertEquals(uiState.hideFromRecents, darkModeUpdated.hideFromRecents)
    assertEquals(uiState.dynamicNotification, darkModeUpdated.dynamicNotification)

    assertEquals(true, hideAppIconUpdated.hideAppIcon)
    assertEquals(uiState.autoRestart, hideAppIconUpdated.autoRestart)
    assertEquals(uiState.darkMode, hideAppIconUpdated.darkMode)
    assertEquals(uiState.hideFromRecents, hideAppIconUpdated.hideFromRecents)
    assertEquals(uiState.dynamicNotification, hideAppIconUpdated.dynamicNotification)

    assertEquals(true, hideFromRecentsUpdated.hideFromRecents)
    assertEquals(uiState.autoRestart, hideFromRecentsUpdated.autoRestart)
    assertEquals(uiState.darkMode, hideFromRecentsUpdated.darkMode)
    assertEquals(uiState.hideAppIcon, hideFromRecentsUpdated.hideAppIcon)
    assertEquals(uiState.dynamicNotification, hideFromRecentsUpdated.dynamicNotification)

    assertEquals(false, dynamicNotificationUpdated.dynamicNotification)
    assertEquals(uiState.autoRestart, dynamicNotificationUpdated.autoRestart)
    assertEquals(uiState.darkMode, dynamicNotificationUpdated.darkMode)
    assertEquals(uiState.hideAppIcon, dynamicNotificationUpdated.hideAppIcon)
    assertEquals(uiState.hideFromRecents, dynamicNotificationUpdated.hideFromRecents)
  }

  @Test
  fun mapsAppSettingsComponentStateRules() {
    assertEquals(true, isAppSettingsAutoRestartEnabled(AppComponentEnabledState.Enabled))
    assertEquals(false, isAppSettingsAutoRestartEnabled(AppComponentEnabledState.Disabled))
    assertEquals(false, isAppSettingsAutoRestartEnabled(AppComponentEnabledState.Unspecified))

    assertEquals(
      AppComponentEnabledState.Enabled,
      appComponentEnabledStateFromPlatformState(
        platformState = 1,
        enabledState = 1,
        disabledState = 2,
      ),
    )
    assertEquals(
      AppComponentEnabledState.Disabled,
      appComponentEnabledStateFromPlatformState(
        platformState = 2,
        enabledState = 1,
        disabledState = 2,
      ),
    )
    assertEquals(
      AppComponentEnabledState.Unspecified,
      appComponentEnabledStateFromPlatformState(
        platformState = 0,
        enabledState = 1,
        disabledState = 2,
      ),
    )
    assertEquals(
      1,
      appComponentEnabledPlatformState(
        componentState = AppComponentEnabledState.Enabled,
        enabledState = 1,
        disabledState = 2,
        defaultState = 0,
      ),
    )
    assertEquals(
      2,
      appComponentEnabledPlatformState(
        componentState = AppComponentEnabledState.Disabled,
        enabledState = 1,
        disabledState = 2,
        defaultState = 0,
      ),
    )
    assertEquals(
      0,
      appComponentEnabledPlatformState(
        componentState = AppComponentEnabledState.Unspecified,
        enabledState = 1,
        disabledState = 2,
        defaultState = 0,
      ),
    )

    assertEquals(
      AppComponentEnabledState.Enabled,
      appSettingsAutoRestartComponentState(autoRestart = true),
    )
    assertEquals(
      AppComponentEnabledState.Disabled,
      appSettingsAutoRestartComponentState(autoRestart = false),
    )
    assertEquals(
      AppComponentEnabledState.Disabled,
      appSettingsHideAppIconComponentState(hideAppIcon = true),
    )
    assertEquals(
      AppComponentEnabledState.Enabled,
      appSettingsHideAppIconComponentState(hideAppIcon = false),
    )
  }

  @Test
  fun createsInitialNetworkSettingsState() {
    assertEquals(
      networkSettingsUiState(),
      networkSettingsInitialUiState(
        hasSystemProxyOption = true,
        enableVpn = true,
        bypassPrivateNetwork = true,
        dnsHijacking = true,
        allowBypass = true,
        allowIpv6 = false,
        systemProxy = true,
        tunStackMode = "system",
        accessControlMode = AccessControlMode.AcceptAll,
      ),
    )
  }

  @Test
  fun updatesNetworkSettingsBooleanFieldsAndPreservesOtherValues() {
    val uiState = networkSettingsUiState()

    val enableVpnUpdated = updateNetworkSettingsEnableVpn(uiState, false)
    val bypassUpdated = updateNetworkSettingsBypassPrivateNetwork(uiState, false)
    val dnsHijackingUpdated = updateNetworkSettingsDnsHijacking(uiState, false)
    val allowBypassUpdated = updateNetworkSettingsAllowBypass(uiState, false)
    val allowIpv6Updated = updateNetworkSettingsAllowIpv6(uiState, true)
    val systemProxyUpdated = updateNetworkSettingsSystemProxy(uiState, false)

    assertEquals(false, enableVpnUpdated.enableVpn)
    assertEquals(uiState.hasSystemProxyOption, enableVpnUpdated.hasSystemProxyOption)
    assertEquals(uiState.bypassPrivateNetwork, enableVpnUpdated.bypassPrivateNetwork)
    assertEquals(uiState.tunStackMode, enableVpnUpdated.tunStackMode)
    assertEquals(uiState.accessControlMode, enableVpnUpdated.accessControlMode)

    assertEquals(false, bypassUpdated.bypassPrivateNetwork)
    assertEquals(uiState.enableVpn, bypassUpdated.enableVpn)
    assertEquals(uiState.dnsHijacking, bypassUpdated.dnsHijacking)
    assertEquals(uiState.tunStackMode, bypassUpdated.tunStackMode)
    assertEquals(uiState.accessControlMode, bypassUpdated.accessControlMode)

    assertEquals(false, dnsHijackingUpdated.dnsHijacking)
    assertEquals(uiState.enableVpn, dnsHijackingUpdated.enableVpn)
    assertEquals(uiState.bypassPrivateNetwork, dnsHijackingUpdated.bypassPrivateNetwork)
    assertEquals(uiState.tunStackMode, dnsHijackingUpdated.tunStackMode)
    assertEquals(uiState.accessControlMode, dnsHijackingUpdated.accessControlMode)

    assertEquals(false, allowBypassUpdated.allowBypass)
    assertEquals(uiState.enableVpn, allowBypassUpdated.enableVpn)
    assertEquals(uiState.allowIpv6, allowBypassUpdated.allowIpv6)
    assertEquals(uiState.tunStackMode, allowBypassUpdated.tunStackMode)
    assertEquals(uiState.accessControlMode, allowBypassUpdated.accessControlMode)

    assertEquals(true, allowIpv6Updated.allowIpv6)
    assertEquals(uiState.enableVpn, allowIpv6Updated.enableVpn)
    assertEquals(uiState.allowBypass, allowIpv6Updated.allowBypass)
    assertEquals(uiState.tunStackMode, allowIpv6Updated.tunStackMode)
    assertEquals(uiState.accessControlMode, allowIpv6Updated.accessControlMode)

    assertEquals(false, systemProxyUpdated.systemProxy)
    assertEquals(uiState.hasSystemProxyOption, systemProxyUpdated.hasSystemProxyOption)
    assertEquals(uiState.enableVpn, systemProxyUpdated.enableVpn)
    assertEquals(uiState.tunStackMode, systemProxyUpdated.tunStackMode)
    assertEquals(uiState.accessControlMode, systemProxyUpdated.accessControlMode)
  }

  @Test
  fun updatesNetworkSettingsModeFieldsAndPreservesOtherValues() {
    val uiState = networkSettingsUiState()

    val tunStackModeUpdated = updateNetworkSettingsTunStackMode(uiState, "mixed")
    val accessControlModeUpdated =
      updateNetworkSettingsAccessControlMode(uiState, AccessControlMode.DenySelected)

    assertEquals("mixed", tunStackModeUpdated.tunStackMode)
    assertEquals(uiState.hasSystemProxyOption, tunStackModeUpdated.hasSystemProxyOption)
    assertEquals(uiState.enableVpn, tunStackModeUpdated.enableVpn)
    assertEquals(uiState.systemProxy, tunStackModeUpdated.systemProxy)
    assertEquals(uiState.accessControlMode, tunStackModeUpdated.accessControlMode)

    assertEquals(AccessControlMode.DenySelected, accessControlModeUpdated.accessControlMode)
    assertEquals(uiState.hasSystemProxyOption, accessControlModeUpdated.hasSystemProxyOption)
    assertEquals(uiState.enableVpn, accessControlModeUpdated.enableVpn)
    assertEquals(uiState.systemProxy, accessControlModeUpdated.systemProxy)
    assertEquals(uiState.tunStackMode, accessControlModeUpdated.tunStackMode)
  }

  @Test
  fun systemProxyOptionAvailabilityUsesPlatformSdkThreshold() {
    assertEquals(
      false,
      networkSettingsHasSystemProxyOptionFromPlatformSdk(
        platformSdk = 28,
        systemProxyMinimumPlatformSdk = 29,
      ),
    )
    assertEquals(
      true,
      networkSettingsHasSystemProxyOptionFromPlatformSdk(
        platformSdk = 29,
        systemProxyMinimumPlatformSdk = 29,
      ),
    )
    assertEquals(
      true,
      networkSettingsHasSystemProxyOptionFromPlatformSdk(
        platformSdk = 30,
        systemProxyMinimumPlatformSdk = 29,
      ),
    )
  }

  private fun appSettingsUiState(): AppSettingsUiState {
    return AppSettingsUiState(
      autoRestart = true,
      darkMode = DarkMode.Auto,
      hideAppIcon = false,
      hideFromRecents = false,
      dynamicNotification = true,
    )
  }

  private fun networkSettingsUiState(): NetworkSettingsUiState {
    return NetworkSettingsUiState(
      hasSystemProxyOption = true,
      enableVpn = true,
      bypassPrivateNetwork = true,
      dnsHijacking = true,
      allowBypass = true,
      allowIpv6 = false,
      systemProxy = true,
      tunStackMode = "system",
      accessControlMode = AccessControlMode.AcceptAll,
    )
  }
}
