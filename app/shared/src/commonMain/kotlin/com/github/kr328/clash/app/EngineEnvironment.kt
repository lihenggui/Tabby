package com.github.kr328.clash.app

import com.github.kr328.clash.crash.model.CrashLogRepository
import com.github.kr328.clash.engine.api.EngineController
import com.github.kr328.clash.engine.api.LogRepository
import com.github.kr328.clash.engine.api.ProfileRepository
import com.github.kr328.clash.log.model.LogFileRepository
import com.github.kr328.clash.settingsstore.TabbyAccessControlSettingsRepository
import com.github.kr328.clash.settingsstore.TabbyAppSettingsRepository
import com.github.kr328.clash.settingsstore.TabbyNetworkSettingsRepository

data class EngineEnvironment(
  val engineController: EngineController,
  val profileRepository: ProfileRepository,
  val logRepository: LogRepository,
  val logFileRepository: LogFileRepository,
  val crashLogRepository: CrashLogRepository,
  val appSettingsRepository: TabbyAppSettingsRepository,
  val networkSettingsRepository: TabbyNetworkSettingsRepository,
  val accessControlSettingsRepository: TabbyAccessControlSettingsRepository,
)
