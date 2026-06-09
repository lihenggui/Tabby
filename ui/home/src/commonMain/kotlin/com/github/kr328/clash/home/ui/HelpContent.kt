package com.github.kr328.clash.home.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineMihomo
import com.github.kr328.clash.ui.icon.BaselineUpdate
import com.github.kr328.clash.ui.icon.OutlineInfo
import com.github.kr328.clash.ui.icon.TabbyIcons
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory
import org.jetbrains.compose.resources.stringResource
import tabby.ui.home.generated.resources.Res as HomeRes
import tabby.ui.home.generated.resources.about
import tabby.ui.home.generated.resources.app_version
import tabby.ui.home.generated.resources.check_for_updates
import tabby.ui.home.generated.resources.core_version
import tabby.ui.home.generated.resources.document
import tabby.ui.home.generated.resources.help
import tabby.ui.home.generated.resources.mihomo_core
import tabby.ui.home.generated.resources.mihomo_wiki
import tabby.ui.home.generated.resources.sources

internal data class HelpContentState(
  val checkingForUpdates: Boolean = false,
  val appVersion: String = "",
  val coreVersion: String = "",
)

data class HelpVersionInfo(
  val appVersion: String,
  val coreVersion: String,
)

internal data class HelpCopyVersionPayload(
  val label: String,
  val text: String,
)

internal fun helpInitialContentState(): HelpContentState {
  return HelpContentState()
}

internal fun HelpContentState.withUpdateCheckStarted(): HelpContentState {
  return copy(checkingForUpdates = true)
}

internal fun HelpContentState.withUpdateCheckFinished(): HelpContentState {
  return copy(checkingForUpdates = false)
}

internal fun HelpContentState.withVersionInfo(
  appVersion: String,
  coreVersion: String,
): HelpContentState {
  return copy(appVersion = appVersion, coreVersion = coreVersion)
}

internal fun HelpContentState.withVersionInfo(versionInfo: HelpVersionInfo): HelpContentState {
  return withVersionInfo(appVersion = versionInfo.appVersion, coreVersion = versionInfo.coreVersion)
}

internal fun helpCopyVersionPayload(version: String): HelpCopyVersionPayload {
  return HelpCopyVersionPayload(
    label = "version",
    text = version,
  )
}

internal fun formatAppVersionInfo(versionName: String?, buildCommit: String): String {
  return "$versionName - $buildCommit"
}

internal fun helpVersionInfo(
  versionName: String?,
  buildCommit: String,
  coreVersion: String,
): HelpVersionInfo {
  return HelpVersionInfo(
    appVersion = formatAppVersionInfo(versionName = versionName, buildCommit = buildCommit),
    coreVersion = coreVersion,
  )
}

@Composable
internal fun HelpContent(
  uiState: HelpContentState,
  tipsText: AnnotatedString,
  appName: String,
  appIconPainter: Painter,
  mihomoWikiUrl: String,
  mihomoCoreUrl: String,
  tabbyUrl: String,
  onOpenLink: (String) -> Unit,
  onCopyVersion: (String) -> Unit,
  onCheckForUpdates: () -> Unit,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState? = null,
) {
  TabbyScaffold(
    title = stringResource(HomeRes.string.help),
    modifier = modifier,
    snackbarHostState = snackbarHostState,
  ) { innerPadding ->
    ProvidePreferenceLocals {
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        preference(
          key = "tips",
          title = {},
          summary = { Text(tipsText) },
          icon = { Icon(imageVector = TabbyIcons.OutlineInfo, contentDescription = null) },
        )
        preferenceCategory(
          key = "cat_document",
          title = { Text(stringResource(HomeRes.string.document)) },
        )
        preference(
          key = "mihomo_wiki",
          title = { Text(stringResource(HomeRes.string.mihomo_wiki)) },
          summary = { Text(mihomoWikiUrl) },
          onClick = { onOpenLink(mihomoWikiUrl) },
        )
        preferenceCategory(
          key = "cat_sources",
          title = { Text(stringResource(HomeRes.string.sources)) },
        )
        preference(
          key = "mihomo_core",
          title = { Text(stringResource(HomeRes.string.mihomo_core)) },
          summary = { Text(mihomoCoreUrl) },
          onClick = { onOpenLink(mihomoCoreUrl) },
        )
        preference(
          key = "tabby",
          title = { Text(appName) },
          summary = { Text(tabbyUrl) },
          onClick = { onOpenLink(tabbyUrl) },
        )
        preferenceCategory(
          key = "cat_update",
          title = { Text(stringResource(HomeRes.string.about)) },
        )
        preference(
          key = "app_version",
          title = { Text(stringResource(HomeRes.string.app_version)) },
          summary = { Text(uiState.appVersion) },
          icon = { Icon(painter = appIconPainter, contentDescription = null) },
          modifier =
            Modifier.combinedClickable(
              onClick = {},
              onLongClick = { onCopyVersion(uiState.appVersion) },
            ),
        )
        preference(
          key = "core_version",
          title = { Text(stringResource(HomeRes.string.core_version)) },
          summary = { Text(uiState.coreVersion) },
          icon = {
            Icon(
              imageVector = TabbyIcons.BaselineMihomo,
              contentDescription = null,
            )
          },
          modifier =
            Modifier.combinedClickable(
              onClick = {},
              onLongClick = { onCopyVersion(uiState.coreVersion) },
            ),
        )
        preference(
          key = "check_for_updates",
          title = { Text(stringResource(HomeRes.string.check_for_updates)) },
          icon = {
            if (uiState.checkingForUpdates) {
              CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
              Icon(imageVector = TabbyIcons.BaselineUpdate, contentDescription = null)
            }
          },
          onClick = onCheckForUpdates,
        )
      }
    }
  }
}
