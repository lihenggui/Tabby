package com.github.kr328.clash.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.github.kr328.clash.core.model.DarkMode
import com.github.kr328.clash.crash.CrashRoute
import com.github.kr328.clash.crash.crashEntries
import com.github.kr328.clash.home.HomeRoute
import com.github.kr328.clash.home.homeEntries
import com.github.kr328.clash.log.LogRoute
import com.github.kr328.clash.log.LogRouteContent
import com.github.kr328.clash.log.logsEntries
import com.github.kr328.clash.profile.ProfilesRoute
import com.github.kr328.clash.profile.ProfilesRouteContent
import com.github.kr328.clash.profile.profilesEntries
import com.github.kr328.clash.proxy.ProxyRoute
import com.github.kr328.clash.proxy.proxyEntries
import com.github.kr328.clash.settings.SettingsRoute
import com.github.kr328.clash.settings.SettingsRouteContent
import com.github.kr328.clash.settings.settingsEntries
import com.github.kr328.clash.ui.nav.addIfNotLast

@Composable
fun PlaceholderTabbyApp(
  engineEnvironment: EngineEnvironment,
  darkMode: DarkMode = DarkMode.Auto,
  modifier: Modifier = Modifier,
) {
  val backStack = remember { mutableStateListOf<NavKey>(HomeRoute.Home) }
  val tunnelState by engineEnvironment.engineController.state.collectAsState()
  val entryProvider =
    remember(tunnelState.mode) {
      entryProvider<NavKey> {
        homeEntries(
          homeContent = {
            PlaceholderHomeScreen(
              engineMode = tunnelState.mode.name,
              onOpenProxy = { backStack.addIfNotLast(ProxyRoute.Proxy) },
              onOpenProfiles = { backStack.addIfNotLast(ProfilesRoute.Profiles()) },
              onOpenProviders = { backStack.addIfNotLast(ProfilesRoute.Providers) },
              onOpenLogs = { backStack.addIfNotLast(LogRoute.Root) },
              onOpenSettings = { backStack.addIfNotLast(SettingsRoute.Root) },
              onOpenHelp = { backStack.addIfNotLast(HomeRoute.Help) },
              onOpenCrash = { backStack.addIfNotLast(CrashRoute.AppCrashed) },
            )
          },
          helpContent = { PlaceholderScreen("Help") },
        )
        proxyEntries(proxyContent = { PlaceholderScreen("Proxy") })
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
        crashEntries(
          apkBrokenContent = { PlaceholderScreen("APK broken") },
          appCrashedContent = { PlaceholderScreen("App crashed") },
        )
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
      }
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
private fun PlaceholderHomeScreen(
  engineMode: String,
  onOpenProxy: () -> Unit,
  onOpenProfiles: () -> Unit,
  onOpenProviders: () -> Unit,
  onOpenLogs: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenHelp: () -> Unit,
  onOpenCrash: () -> Unit,
  modifier: Modifier = Modifier,
) {
  PlaceholderScreen(
    title = "Tabby",
    "Proxy" to onOpenProxy,
    "Profiles" to onOpenProfiles,
    "Providers" to onOpenProviders,
    "Logs" to onOpenLogs,
    "Settings" to onOpenSettings,
    "Help" to onOpenHelp,
    "Crash" to onOpenCrash,
    subtitle = "Engine mode: $engineMode",
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
