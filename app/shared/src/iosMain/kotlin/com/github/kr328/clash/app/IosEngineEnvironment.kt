package com.github.kr328.clash.app

import com.github.kr328.clash.engine.ios.IosEngineController
import com.github.kr328.clash.engine.ios.IosLogRepository
import com.github.kr328.clash.engine.ios.IosProfileRepository
import com.github.kr328.clash.settingsstore.asStoreProvider
import com.russhwolf.settings.NSUserDefaultsSettings
import platform.Foundation.NSUserDefaults

fun iosEngineEnvironment(): EngineEnvironment {
  val uiStoreProvider = iosSettingsStoreProvider()
  val serviceStoreProvider = iosSettingsStoreProvider()

  return EngineEnvironment(
    engineController = IosEngineController(),
    profileRepository = IosProfileRepository(),
    logRepository = IosLogRepository(),
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

private fun iosSettingsStoreProvider() =
  NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults).asStoreProvider()
