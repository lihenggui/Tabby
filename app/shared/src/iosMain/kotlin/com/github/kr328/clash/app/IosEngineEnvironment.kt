package com.github.kr328.clash.app

import com.github.kr328.clash.crash.model.emptyCrashLogRepository
import com.github.kr328.clash.engine.ios.IosEngineController
import com.github.kr328.clash.engine.ios.IosLogRepository
import com.github.kr328.clash.engine.ios.IosProfileRepository
import com.github.kr328.clash.log.model.emptyLogFileRepository
import com.github.kr328.clash.settingsstore.TabbyAccessControlSettingsRepository
import com.github.kr328.clash.settingsstore.TabbyAppSettingsRepository
import com.github.kr328.clash.settingsstore.TabbyNetworkSettingsRepository
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
    logFileRepository = emptyLogFileRepository(),
    crashLogRepository = emptyCrashLogRepository(),
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
    accessControlSettingsRepository =
      TabbyAccessControlSettingsRepository(
        uiStoreProvider = uiStoreProvider,
        serviceStoreProvider = serviceStoreProvider,
      ),
  )
}

private fun iosSettingsStoreProvider() =
  NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults).asStoreProvider()
