package com.github.kr328.clash.app

import com.github.kr328.clash.engine.ios.IosEngineController
import com.github.kr328.clash.engine.ios.IosLogRepository
import com.github.kr328.clash.engine.ios.IosProfileRepository

fun iosEngineEnvironment(): EngineEnvironment =
  EngineEnvironment(
    engineController = IosEngineController(),
    profileRepository = IosProfileRepository(),
    logRepository = IosLogRepository(),
  )
