package com.github.kr328.clash.app

import com.github.kr328.clash.engine.ios.IosEngineController
import com.github.kr328.clash.engine.ios.IosLogRepository
import com.github.kr328.clash.engine.ios.IosProfileRepository
import com.github.kr328.clash.settingsstore.asStoreProvider
import com.russhwolf.settings.NSUserDefaultsSettings
import platform.Foundation.NSUserDefaults

fun iosEngineEnvironment(): EngineEnvironment =
  EngineEnvironment(
    engineController = IosEngineController(),
    profileRepository = IosProfileRepository(),
    logRepository = IosLogRepository(),
    appSettingsRepository =
      TabbyAppSettingsRepository(
        uiStoreProvider = iosSettingsStoreProvider(),
        serviceStoreProvider = iosSettingsStoreProvider(),
      ),
  )

private fun iosSettingsStoreProvider() =
  NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults).asStoreProvider()
