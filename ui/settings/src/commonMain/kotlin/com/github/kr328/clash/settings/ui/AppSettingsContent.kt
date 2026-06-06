package com.github.kr328.clash.settings.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import com.github.kr328.clash.core.model.DarkMode
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineBrightness4
import com.github.kr328.clash.ui.icon.BaselineDomain
import com.github.kr328.clash.ui.icon.BaselineHide
import com.github.kr328.clash.ui.icon.BaselineRestore
import com.github.kr328.clash.ui.icon.BaselineStack
import com.github.kr328.clash.ui.icon.TabbyIcons
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.preferenceCategory
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tabby.ui.settings.generated.resources.Res
import tabby.ui.settings.generated.resources.allow_tabby_auto_restart
import tabby.ui.settings.generated.resources.always_dark
import tabby.ui.settings.generated.resources.always_light
import tabby.ui.settings.generated.resources.app
import tabby.ui.settings.generated.resources.auto_restart
import tabby.ui.settings.generated.resources.behavior
import tabby.ui.settings.generated.resources.dark_mode
import tabby.ui.settings.generated.resources.follow_system
import tabby.ui.settings.generated.resources.hide_app_icon_desc
import tabby.ui.settings.generated.resources.hide_app_icon_title
import tabby.ui.settings.generated.resources.hide_from_recents_desc
import tabby.ui.settings.generated.resources.hide_from_recents_title
import tabby.ui.settings.generated.resources.interface_
import tabby.ui.settings.generated.resources.service
import tabby.ui.settings.generated.resources.show_traffic
import tabby.ui.settings.generated.resources.show_traffic_summary

internal data class AppSettingsUiState(
  val autoRestart: Boolean,
  val darkMode: DarkMode,
  val hideAppIcon: Boolean,
  val hideFromRecents: Boolean,
  val dynamicNotification: Boolean,
)

@Composable
internal fun AppSettingsContent(
  clashRunning: Boolean,
  uiState: AppSettingsUiState,
  onAutoRestartChange: (Boolean) -> Unit,
  onDarkModeChange: (DarkMode) -> Unit,
  onHideAppIconChange: (Boolean) -> Unit,
  onHideFromRecentsChange: (Boolean) -> Unit,
  onDynamicNotificationChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  TabbyScaffold(title = stringResource(Res.string.app), modifier = modifier) { innerPadding ->
    ProvidePreferenceLocals {
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        preferenceCategory(
          key = "cat_behavior",
          title = { Text(stringResource(Res.string.behavior)) },
        )
        item(key = "auto_restart") {
          SwitchPreference(
            value = uiState.autoRestart,
            onValueChange = onAutoRestartChange,
            icon = { Icon(imageVector = TabbyIcons.BaselineRestore, contentDescription = null) },
            title = { Text(stringResource(Res.string.auto_restart)) },
            summary = { Text(stringResource(Res.string.allow_tabby_auto_restart)) },
          )
        }
        preferenceCategory(
          key = "cat_interface",
          title = { Text(stringResource(Res.string.interface_)) },
        )
        item(key = "dark_mode") {
          ListPreference(
            value = uiState.darkMode,
            onValueChange = onDarkModeChange,
            values = listOf(DarkMode.Auto, DarkMode.ForceLight, DarkMode.ForceDark),
            icon = {
              Icon(imageVector = TabbyIcons.BaselineBrightness4, contentDescription = null)
            },
            title = { Text(stringResource(Res.string.dark_mode)) },
            summary = { Text(stringResource(uiState.darkMode.summaryStringResource)) },
            valueToText = { AnnotatedString(stringResource(it.summaryStringResource)) },
          )
        }
        item(key = "hide_app_icon") {
          SwitchPreference(
            value = uiState.hideAppIcon,
            onValueChange = onHideAppIconChange,
            icon = { Icon(imageVector = TabbyIcons.BaselineHide, contentDescription = null) },
            title = { Text(stringResource(Res.string.hide_app_icon_title)) },
            summary = { Text(stringResource(Res.string.hide_app_icon_desc)) },
          )
        }
        item(key = "hide_from_recents") {
          SwitchPreference(
            value = uiState.hideFromRecents,
            onValueChange = onHideFromRecentsChange,
            icon = { Icon(imageVector = TabbyIcons.BaselineStack, contentDescription = null) },
            title = { Text(stringResource(Res.string.hide_from_recents_title)) },
            summary = { Text(stringResource(Res.string.hide_from_recents_desc)) },
          )
        }
        preferenceCategory(
          key = "cat_service",
          title = { Text(stringResource(Res.string.service)) },
        )
        item(key = "show_traffic") {
          SwitchPreference(
            value = uiState.dynamicNotification,
            onValueChange = onDynamicNotificationChange,
            enabled = !clashRunning,
            icon = { Icon(imageVector = TabbyIcons.BaselineDomain, contentDescription = null) },
            title = { Text(stringResource(Res.string.show_traffic)) },
            summary = { Text(stringResource(Res.string.show_traffic_summary)) },
          )
        }
      }
    }
  }
}

private val DarkMode.summaryStringResource: StringResource
  get() =
    when (this) {
      DarkMode.Auto -> Res.string.follow_system
      DarkMode.ForceLight -> Res.string.always_light
      DarkMode.ForceDark -> Res.string.always_dark
    }
