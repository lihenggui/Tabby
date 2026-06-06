package com.github.kr328.clash.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.github.kr328.clash.settings.ui.SettingsScreen
import com.github.kr328.clash.ui.nav.TabbyNavDisplay
import com.github.kr328.clash.ui.nav.addIfNotLast

fun EntryProviderScope<NavKey>.settingsEntries(rootContent: @Composable () -> Unit) {
  entry<SettingsRoute.Root> { rootContent() }
}

@Composable
fun SettingsRouteContent(
  appSettingsContent: @Composable () -> Unit,
  networkSettingsContent: @Composable (onStartAccessControlList: () -> Unit) -> Unit,
  overrideSettingsContent: @Composable (onResetCompleted: () -> Unit) -> Unit,
  metaFeatureSettingsContent: @Composable (onResetCompleted: () -> Unit) -> Unit,
  accessControlContent: @Composable () -> Unit,
  settingsContent:
    @Composable
    (
      onOpenAppSettings: () -> Unit,
      onOpenNetworkSettings: () -> Unit,
      onOpenOverrideSettings: () -> Unit,
      onOpenMetaFeatureSettings: () -> Unit,
    ) -> Unit =
    { onOpenAppSettings, onOpenNetworkSettings, onOpenOverrideSettings, onOpenMetaFeatureSettings ->
      SettingsScreen(
        onOpenAppSettings = onOpenAppSettings,
        onOpenNetworkSettings = onOpenNetworkSettings,
        onOpenOverrideSettings = onOpenOverrideSettings,
        onOpenMetaFeatureSettings = onOpenMetaFeatureSettings,
      )
    },
) {
  val backStack = remember { mutableStateListOf<NavKey>(SettingsRoute.Root) }

  TabbyNavDisplay(
    backStack = backStack,
    entryProvider =
      entryProvider<NavKey> {
        entry<SettingsRoute.Root> {
          settingsContent(
            { backStack.addIfNotLast(SettingsRoute.AppSettings) },
            { backStack.addIfNotLast(SettingsRoute.NetworkSettings) },
            { backStack.addIfNotLast(SettingsRoute.OverrideSettings) },
            { backStack.addIfNotLast(SettingsRoute.MetaFeatureSettings) },
          )
        }
        entry<SettingsRoute.AppSettings> { appSettingsContent() }
        entry<SettingsRoute.NetworkSettings> {
          networkSettingsContent { backStack.addIfNotLast(SettingsRoute.AccessControl) }
        }
        entry<SettingsRoute.OverrideSettings> {
          overrideSettingsContent { backStack.removeLastOrNull() }
        }
        entry<SettingsRoute.MetaFeatureSettings> {
          metaFeatureSettingsContent { backStack.removeLastOrNull() }
        }
        entry<SettingsRoute.AccessControl> { accessControlContent() }
      },
  )
}
