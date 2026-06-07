package com.github.kr328.clash.app

import com.github.kr328.clash.engine.desktop.DesktopEngineController
import com.github.kr328.clash.engine.desktop.DesktopLogRepository
import com.github.kr328.clash.engine.desktop.DesktopMihomoApi
import com.github.kr328.clash.engine.desktop.DesktopMihomoBinaryResolver
import com.github.kr328.clash.engine.desktop.DesktopMihomoCliConfigValidator
import com.github.kr328.clash.engine.desktop.DesktopMihomoCommand
import com.github.kr328.clash.engine.desktop.DesktopMihomoEndpoint
import com.github.kr328.clash.engine.desktop.DesktopMihomoProcess
import com.github.kr328.clash.engine.desktop.DesktopProfileRepository
import com.github.kr328.clash.engine.desktop.defaultMihomoBinaryInstallDir
import com.github.kr328.clash.engine.desktop.defaultMihomoHomeDir
import com.github.kr328.clash.settingsstore.asStoreProvider
import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences

fun desktopEngineEnvironment(): EngineEnvironment {
  val endpoint = DesktopMihomoEndpoint(controller = "127.0.0.1:9090")
  val homeDir = defaultMihomoHomeDir()
  val uiStoreProvider = desktopSettingsStoreProvider("settings_ui")
  val serviceStoreProvider = desktopSettingsStoreProvider("settings_service")
  val binary = DesktopMihomoBinaryResolver().prepareExecutable(defaultMihomoBinaryInstallDir())
  val mihomoProcess = binary?.let {
    DesktopMihomoProcess(
      DesktopMihomoCommand(
        binary = it,
        homeDir = homeDir,
        configFile = homeDir.resolve("config.yaml"),
        externalController = endpoint.controller,
        secret = endpoint.secret,
      )
    )
  }
  val mihomoApi = if (mihomoProcess != null) DesktopMihomoApi(endpoint) else null

  return EngineEnvironment(
    engineController =
      DesktopEngineController(
        mihomoProcess = mihomoProcess,
        mihomoApi = mihomoApi,
      ),
    profileRepository =
      DesktopProfileRepository(
        homeDir = homeDir,
        configValidator = binary?.let(::DesktopMihomoCliConfigValidator),
      ),
    logRepository = DesktopLogRepository(mihomoApi),
    appSettingsRepository =
      TabbyAppSettingsRepository(
        uiStoreProvider = uiStoreProvider,
        serviceStoreProvider = serviceStoreProvider,
      ),
    networkSettingsRepository =
      TabbyNetworkSettingsRepository(
        uiStoreProvider = uiStoreProvider,
        serviceStoreProvider = serviceStoreProvider,
      ),
  )
}

private fun desktopSettingsStoreProvider(name: String) =
  PreferencesSettings(Preferences.userRoot().node(DESKTOP_SETTINGS_NODE).node(name))
    .asStoreProvider()

private const val DESKTOP_SETTINGS_NODE = "io.github.goooler.tabby"
