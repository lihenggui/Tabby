package com.github.kr328.clash.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.DarkMode
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.crash.crashEntries
import com.github.kr328.clash.crash.ui.ApkBrokenRouteContent
import com.github.kr328.clash.crash.ui.AppCrashedRouteContent
import com.github.kr328.clash.home.homeEntries
import com.github.kr328.clash.home.ui.HelpRouteContent
import com.github.kr328.clash.home.ui.HomeRouteContent
import com.github.kr328.clash.log.LogRouteContent
import com.github.kr328.clash.log.logsEntries
import com.github.kr328.clash.log.ui.LogcatRouteContent
import com.github.kr328.clash.log.ui.LogsRouteContent
import com.github.kr328.clash.profile.ProfilesRouteContent
import com.github.kr328.clash.profile.profilesEntries
import com.github.kr328.clash.profile.ui.FilesRouteContent
import com.github.kr328.clash.profile.ui.NewProfileRouteContent
import com.github.kr328.clash.profile.ui.ProfilesListRouteContent
import com.github.kr328.clash.profile.ui.PropertiesRouteContent
import com.github.kr328.clash.profile.ui.ProvidersRouteContent
import com.github.kr328.clash.proxy.proxyEntries
import com.github.kr328.clash.proxy.ui.ProxyRouteContent
import com.github.kr328.clash.settings.SettingsRouteContent
import com.github.kr328.clash.settings.settingsEntries
import com.github.kr328.clash.settings.ui.AccessControlRouteContent
import com.github.kr328.clash.settings.ui.AppSettingsRouteContent
import com.github.kr328.clash.settings.ui.MetaFeatureSettingsRouteContent
import com.github.kr328.clash.settings.ui.NetworkSettingsRouteContent
import com.github.kr328.clash.settings.ui.OverrideSettingsRouteContent
import kotlin.uuid.Uuid

@Composable
fun PlaceholderTabbyApp(
  engineEnvironment: EngineEnvironment,
  darkMode: DarkMode = DarkMode.Auto,
  modifier: Modifier = Modifier,
) {
  val appDarkModeState = remember { mutableStateOf(darkMode) }
  val backStack = remember { tabbyInitialBackStack() }
  LaunchedEffect(darkMode) { appDarkModeState.value = darkMode }
  val entryProvider =
    remember(engineEnvironment) {
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
            helpContent = { HelpRouteContent() },
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
                profilesContent = { onOpenCreate, _ ->
                  ProfilesListRouteContent(onCreate = onOpenCreate)
                },
                newProfileContent = { _, onFinish ->
                  NewProfileRouteContent(onCreateBuiltIn = { onFinish() })
                },
                propertiesContent = { uuid, onBrowseFiles, onFinish ->
                  PropertiesRouteContent(
                    profile = defaultProfilePropertiesRouteProfile(uuid),
                    onBrowseFiles = { onBrowseFiles(it.uuid) },
                    onFinish = onFinish,
                  )
                },
                filesContent = { _, onFinish ->
                  FilesRouteContent(onBack = onFinish)
                },
              )
            },
            providersContent = { ProvidersRouteContent() },
          )
        },
        logsEntries = {
          logsEntries {
            LogRouteContent(
              logcatRunning = false,
              logsContent = { onStartLogcat, onOpenFile ->
                LogsRouteContent(
                  logs = emptyList(),
                  formatCreated = { "" },
                  onDeleteAll = {},
                  onStartLogcat = onStartLogcat,
                  onOpenFile = { file -> onOpenFile(file.fileName) },
                  showDeleteAllAction = false,
                )
              },
              logcatContent = { fileName, onOpenLogs, onInvalidFile, onClose ->
                LogcatRouteContent(
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
                AppSettingsRouteContent(
                  darkMode = appDarkModeState.value,
                  onDarkModeChange = { appDarkModeState.value = it },
                )
              },
              networkSettingsContent = { onStartAccessControlList ->
                NetworkSettingsRouteContent(onStartAccessControlList = onStartAccessControlList)
              },
              overrideSettingsContent = { onResetCompleted ->
                OverrideSettingsRouteContent(onResetCompleted = onResetCompleted)
              },
              metaFeatureSettingsContent = { onResetCompleted ->
                MetaFeatureSettingsRouteContent(onResetCompleted = onResetCompleted)
              },
              accessControlContent = { AccessControlRouteContent() },
            )
          }
        },
        crashEntries = {
          crashEntries(
            apkBrokenContent = {
              ApkBrokenRouteContent(
                releasesUrl = TABBY_GITHUB_URL,
                onOpenReleases = {},
              )
            },
            appCrashedContent = { AppCrashedRouteContent(logs = "") },
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

private fun defaultProfilePropertiesRouteProfile(uuid: Uuid): Profile {
  return Profile(
    uuid = uuid,
    name = "Meta Profile",
    type = Profile.Type.Url,
    source = "https://example.com/config.yaml",
    active = false,
    interval = 0,
    upload = 0,
    download = 0,
    total = 0,
    expire = 0,
    updatedAt = 0,
    imported = false,
    pending = false,
  )
}
