package com.github.kr328.clash.home.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.AnnotatedString
import com.github.kr328.clash.ui.icon.BaselineHelpCenter
import com.github.kr328.clash.ui.icon.TabbyIcons

@Composable
fun HelpRouteContent(
  modifier: Modifier = Modifier,
  checkingForUpdates: Boolean = false,
  appVersion: String = "Tabby",
  coreVersion: String = "Mihomo",
  tipsText: AnnotatedString = AnnotatedString(DEFAULT_HELP_TIPS_TEXT),
  appName: String = "Tabby",
  mihomoWikiUrl: String = MIHOMO_WIKI_URL,
  mihomoCoreUrl: String = MIHOMO_CORE_URL,
  tabbyUrl: String = TABBY_GITHUB_URL,
  onOpenLink: (String) -> Unit = {},
  onCopyVersion: (String) -> Unit = {},
  onCheckForUpdates: () -> Unit = {},
) {
  HelpContent(
    modifier = modifier,
    uiState =
      HelpContentState(
        checkingForUpdates = checkingForUpdates,
        appVersion = appVersion,
        coreVersion = coreVersion,
      ),
    tipsText = tipsText,
    appName = appName,
    appIconPainter = rememberVectorPainter(TabbyIcons.BaselineHelpCenter),
    mihomoWikiUrl = mihomoWikiUrl,
    mihomoCoreUrl = mihomoCoreUrl,
    tabbyUrl = tabbyUrl,
    snackbarHostState = remember { SnackbarHostState() },
    onOpenLink = onOpenLink,
    onCopyVersion = onCopyVersion,
    onCheckForUpdates = onCheckForUpdates,
  )
}

private const val DEFAULT_HELP_TIPS_TEXT = "Tabby is freeware and does not provide a proxy service."
private const val MIHOMO_WIKI_URL = "https://wiki.metacubex.one/"
private const val MIHOMO_CORE_URL = "https://github.com/MetaCubeX/mihomo"
private const val TABBY_GITHUB_URL = "https://github.com/Goooler/Tabby"
