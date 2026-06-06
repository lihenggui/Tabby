package com.github.kr328.clash.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.core.model.DarkMode
import com.github.kr328.clash.crash.crashEntries
import com.github.kr328.clash.crash.ui.ApkBrokenRouteContent
import com.github.kr328.clash.crash.ui.AppCrashedRouteContent
import com.github.kr328.clash.home.homeEntries
import com.github.kr328.clash.home.ui.HomeRouteContent
import com.github.kr328.clash.log.LogRouteContent
import com.github.kr328.clash.log.logsEntries
import com.github.kr328.clash.log.ui.LogsRouteContent
import com.github.kr328.clash.profile.ProfilesRouteContent
import com.github.kr328.clash.profile.profilesEntries
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
            helpContent = { PlaceholderScreen("Help") },
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
                  PlaceholderScreen("Profiles", "New profile" to onOpenCreate)
                },
                newProfileContent = { _, onFinish ->
                  PlaceholderScreen("New profile", "Done" to onFinish)
                },
                propertiesContent = { uuid, onBrowseFiles, onFinish ->
                  PlaceholderScreen(
                    title = "Profile properties",
                    "Files" to { onBrowseFiles(uuid) },
                    "Done" to { onFinish(true) },
                  )
                },
                filesContent = { _, onFinish ->
                  PlaceholderScreen("Profile files", "Done" to onFinish)
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
              logcatContent = { _, onOpenLogs, _, onClose ->
                PlaceholderScreen(
                  title = "Logcat",
                  "Logs" to onOpenLogs,
                  "Close" to onClose,
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

@Composable
private fun PlaceholderScreen(
  title: String,
  vararg actions: Pair<String, () -> Unit>,
  subtitle: String? = null,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxSize().padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(text = "Tabby", style = MaterialTheme.typography.titleLarge)
    Text(text = title, style = MaterialTheme.typography.bodyLarge)
    subtitle?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
    actions.forEach { (label, onClick) ->
      Button(onClick = onClick) { Text(text = label) }
    }
  }
}
