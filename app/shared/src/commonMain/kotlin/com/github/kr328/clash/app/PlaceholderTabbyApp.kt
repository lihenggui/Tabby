package com.github.kr328.clash.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.core.model.DarkMode
import com.github.kr328.clash.crash.crashEntries
import com.github.kr328.clash.home.homeEntries
import com.github.kr328.clash.home.ui.HomeRouteContent
import com.github.kr328.clash.log.LogRouteContent
import com.github.kr328.clash.log.logsEntries
import com.github.kr328.clash.profile.ProfilesRouteContent
import com.github.kr328.clash.profile.profilesEntries
import com.github.kr328.clash.proxy.proxyEntries
import com.github.kr328.clash.settings.SettingsRouteContent
import com.github.kr328.clash.settings.settingsEntries

@Composable
fun PlaceholderTabbyApp(
  engineEnvironment: EngineEnvironment,
  darkMode: DarkMode = DarkMode.Auto,
  modifier: Modifier = Modifier,
) {
  val backStack = remember { tabbyInitialBackStack() }
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
        proxyEntries = { proxyEntries(proxyContent = { PlaceholderScreen("Proxy") }) },
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
            providersContent = { PlaceholderScreen("Providers") },
          )
        },
        logsEntries = {
          logsEntries {
            LogRouteContent(
              logcatRunning = false,
              logsContent = { onStartLogcat, _ ->
                PlaceholderScreen("Logs", "Logcat" to onStartLogcat)
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
              appSettingsContent = { PlaceholderScreen("App settings") },
              networkSettingsContent = { onStartAccessControlList ->
                PlaceholderScreen(
                  title = "Network settings",
                  "Access control" to onStartAccessControlList,
                )
              },
              overrideSettingsContent = { onResetCompleted ->
                PlaceholderScreen("Override settings", "Done" to onResetCompleted)
              },
              metaFeatureSettingsContent = { onResetCompleted ->
                PlaceholderScreen("Meta feature settings", "Done" to onResetCompleted)
              },
              accessControlContent = { PlaceholderScreen("Access control") },
            )
          }
        },
        crashEntries = {
          crashEntries(
            apkBrokenContent = { PlaceholderScreen("APK broken") },
            appCrashedContent = { PlaceholderScreen("App crashed") },
          )
        },
      )
    }

  TabbyApp(
    darkMode = darkMode,
    backStack = backStack,
    entryProvider = entryProvider,
    onBack = { backStack.removeLastOrNull() },
    modifier = modifier,
  )
}

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
