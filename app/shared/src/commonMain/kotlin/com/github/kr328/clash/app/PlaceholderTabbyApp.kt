package com.github.kr328.clash.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.github.kr328.clash.core.model.DarkMode
import com.github.kr328.clash.crash.crashEntries
import com.github.kr328.clash.crash.ui.ApkBrokenRouteContent
import com.github.kr328.clash.home.homeEntries
import com.github.kr328.clash.home.ui.HelpRouteContent
import com.github.kr328.clash.home.ui.HomeRouteContent
import com.github.kr328.clash.log.LogRouteContent
import com.github.kr328.clash.log.logsEntries
import com.github.kr328.clash.profile.ProfilesRouteContent
import com.github.kr328.clash.profile.profilesEntries
import com.github.kr328.clash.profile.ui.FilesRouteContent
import com.github.kr328.clash.proxy.proxyEntries
import com.github.kr328.clash.proxy.ui.ProxyRouteContent
import com.github.kr328.clash.settings.SettingsRouteContent
import com.github.kr328.clash.settings.settingsEntries
import com.github.kr328.clash.settingsstore.TabbyAppSettings

@Composable
fun PlaceholderTabbyApp(
  engineEnvironment: EngineEnvironment,
  darkMode: DarkMode = DarkMode.Auto,
  modifier: Modifier = Modifier,
) {
  val appSettingsRepository = engineEnvironment.appSettingsRepository
  val networkSettingsRepository = engineEnvironment.networkSettingsRepository
  val accessControlSettingsRepository = engineEnvironment.accessControlSettingsRepository
  val uriHandler = LocalUriHandler.current
  val appDarkModeState =
    remember(appSettingsRepository) {
      mutableStateOf(
        appSettingsRepository.query(defaults = TabbyAppSettings(darkMode = darkMode)).darkMode
      )
    }
  val backStack = remember { tabbyInitialBackStack() }
  LaunchedEffect(appSettingsRepository, darkMode) {
    if (!appSettingsRepository.hasDarkMode) {
      appDarkModeState.value = darkMode
    }
  }
  val entryProvider =
    remember(engineEnvironment, uriHandler) {
      tabbyEntryProvider(
        backStack = backStack,
        homeEntries = { actions ->
          homeEntries(
            homeContent = {
              HomeRouteContent(
                engineController = engineEnvironment.engineController,
                profileRepository = engineEnvironment.profileRepository,
                onOpenProxy = actions.openProxy,
                onOpenProfiles = actions.openProfiles,
                onOpenProviders = actions.openProviders,
                onOpenLogs = actions.openLogs,
                onOpenSettings = actions.openSettings,
                onOpenHelp = actions.openHelp,
              )
            },
            helpContent = { HelpRouteContent(onOpenLink = uriHandler::openUri) },
          )
        },
        proxyEntries = { onReLaunch ->
          proxyEntries(
            proxyContent = {
              ProxyRouteContent(
                engineController = engineEnvironment.engineController,
                onReLaunch = onReLaunch,
              )
            }
          )
        },
        profilesEntries = {
          profilesEntries(
            profilesContent = { key ->
              ProfilesRouteContent(
                route = key,
                profilesContent = { onOpenCreate, onOpenEdit ->
                  ProfileRepositoryProfilesListRouteContent(
                    profileRepository = engineEnvironment.profileRepository,
                    onCreate = onOpenCreate,
                    onEdit = onOpenEdit,
                  )
                },
                newProfileContent = { onProperties, _ ->
                  ProfileRepositoryNewProfileRouteContent(
                    profileRepository = engineEnvironment.profileRepository,
                    onProperties = onProperties,
                  )
                },
                propertiesContent = { uuid, onBrowseFiles, onFinish ->
                  ProfileRepositoryPropertiesRouteContent(
                    profileRepository = engineEnvironment.profileRepository,
                    uuid = uuid,
                    onBrowseFiles = onBrowseFiles,
                    onFinish = onFinish,
                  )
                },
                filesContent = { _, onFinish ->
                  FilesRouteContent(onBack = onFinish)
                },
              )
            },
            providersContent = {
              EngineControllerProvidersRouteContent(
                engineController = engineEnvironment.engineController
              )
            },
          )
        },
        logsEntries = {
          logsEntries {
            LogRouteContent(
              logcatRunning = false,
              logsContent = { onStartLogcat, onOpenFile ->
                LogFileRepositoryLogsRouteContent(
                  logFileRepository = engineEnvironment.logFileRepository,
                  onStartLogcat = onStartLogcat,
                  onOpenFile = onOpenFile,
                )
              },
              logcatContent = { fileName, onOpenLogs, onInvalidFile, onClose ->
                LogRepositoryLogcatRouteContent(
                  logRepository = engineEnvironment.logRepository,
                  fileName = fileName,
                  onOpenLogs = onOpenLogs,
                  onInvalidFile = onInvalidFile,
                  onClose = onClose,
                )
              },
            )
          }
        },
        settingsEntries = {
          settingsEntries {
            SettingsRouteContent(
              appSettingsContent = {
                AppSettingsRepositoryRouteContent(
                  repository = appSettingsRepository,
                  darkMode = appDarkModeState.value,
                  onDarkModeChange = { appDarkModeState.value = it },
                )
              },
              networkSettingsContent = { onStartAccessControlList ->
                NetworkSettingsRepositoryRouteContent(
                  repository = networkSettingsRepository,
                  onStartAccessControlList = onStartAccessControlList,
                )
              },
              overrideSettingsContent = { onResetCompleted ->
                EngineControllerOverrideSettingsRouteContent(
                  engineController = engineEnvironment.engineController,
                  onResetCompleted = onResetCompleted,
                )
              },
              metaFeatureSettingsContent = { onResetCompleted ->
                EngineControllerMetaFeatureSettingsRouteContent(
                  engineController = engineEnvironment.engineController,
                  onResetCompleted = onResetCompleted,
                )
              },
              accessControlContent = {
                AccessControlSettingsRepositoryRouteContent(
                  repository = accessControlSettingsRepository
                )
              },
            )
          }
        },
        crashEntries = {
          crashEntries(
            apkBrokenContent = {
              ApkBrokenRouteContent(
                releasesUrl = TABBY_GITHUB_URL,
                onOpenReleases = { uriHandler.openUri(TABBY_GITHUB_URL) },
              )
            },
            appCrashedContent = {
              CrashLogRepositoryAppCrashedRouteContent(
                crashLogRepository = engineEnvironment.crashLogRepository
              )
            },
          )
        },
      )
    }

  TabbyApp(
    darkMode = appDarkModeState.value,
    backStack = backStack,
    entryProvider = entryProvider,
    onBack = { backStack.removeLastOrNull() },
    modifier = modifier,
  )
}

private const val TABBY_GITHUB_URL = "https://github.com/Goooler/Tabby"
