package com.github.kr328.clash.app

import com.github.kr328.clash.engine.api.EngineController
import com.github.kr328.clash.engine.api.LogRepository
import com.github.kr328.clash.engine.api.ProfileRepository
import com.github.kr328.clash.log.model.LogFileRepository

data class EngineEnvironment(
  val engineController: EngineController,
  val profileRepository: ProfileRepository,
  val logRepository: LogRepository,
  val logFileRepository: LogFileRepository,
  val appSettingsRepository: TabbyAppSettingsRepository,
  val networkSettingsRepository: TabbyNetworkSettingsRepository,
  val accessControlSettingsRepository: TabbyAccessControlSettingsRepository,
)
