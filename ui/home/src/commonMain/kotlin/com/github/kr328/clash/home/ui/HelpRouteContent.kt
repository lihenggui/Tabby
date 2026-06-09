package com.github.kr328.clash.home.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.AnnotatedString
import com.github.kr328.clash.home.MIHOMO_CORE
import com.github.kr328.clash.home.MIHOMO_WIKI
import com.github.kr328.clash.home.TABBY_GITHUB
import com.github.kr328.clash.home.TABBY_RELEASES_LATEST
import com.github.kr328.clash.ui.icon.BaselineHelpCenter
import com.github.kr328.clash.ui.icon.TabbyIcons
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tabby.ui.home.generated.resources.Res as HomeRes
import tabby.ui.home.generated.resources.already_up_to_date
import tabby.ui.home.generated.resources.check_update_failed
import tabby.ui.home.generated.resources.open
import tabby.ui.home.generated.resources.update_available

@Composable
fun HelpRouteContent(
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  checkingForUpdates: Boolean = false,
  appVersion: String = "Tabby",
  coreVersion: String = "Mihomo",
  tipsText: AnnotatedString = AnnotatedString(DEFAULT_HELP_TIPS_TEXT),
  appName: String = "Tabby",
  appIconPainter: Painter = rememberVectorPainter(TabbyIcons.BaselineHelpCenter),
  mihomoWikiUrl: String = MIHOMO_WIKI,
  mihomoCoreUrl: String = MIHOMO_CORE,
  tabbyUrl: String = TABBY_GITHUB,
  onOpenLink: (String) -> Unit = {},
  onCopyVersion: (String) -> Unit = {},
  releasesUrl: String = TABBY_RELEASES_LATEST,
  onLoadVersionInfo: suspend () -> HelpVersionInfo = {
    HelpVersionInfo(appVersion = appVersion, coreVersion = coreVersion)
  },
  onFetchLatestReleaseTag: suspend () -> String? = { null },
  onLoadLocalVersion: suspend () -> String? = { null },
  onUpdateCheckFailure: (Throwable) -> Unit = {},
) {
  var uiState by remember {
    mutableStateOf(
      HelpContentState(
        checkingForUpdates = checkingForUpdates,
        appVersion = appVersion,
        coreVersion = coreVersion,
      )
    )
  }
  var eventState by remember { mutableStateOf<HelpEventState>(helpInitialEventState()) }
  val scope = rememberCoroutineScope()
  val currentOnOpenLink by rememberUpdatedState(onOpenLink)
  val currentOnLoadVersionInfo by rememberUpdatedState(onLoadVersionInfo)
  val currentOnFetchLatestReleaseTag by rememberUpdatedState(onFetchLatestReleaseTag)
  val currentOnLoadLocalVersion by rememberUpdatedState(onLoadLocalVersion)
  val currentOnUpdateCheckFailure by rememberUpdatedState(onUpdateCheckFailure)
  val updateAvailableText = stringResource(HomeRes.string.update_available)
  val openActionText = stringResource(HomeRes.string.open)
  val alreadyUpToDateMessage = stringResource(HomeRes.string.already_up_to_date)
  val updateCheckFailedMessage = stringResource(HomeRes.string.check_update_failed)

  LaunchedEffect(Unit) { uiState = uiState.withVersionInfo(currentOnLoadVersionInfo()) }

  LaunchedEffect(eventState) {
    when (val action = helpEventRouteEffect(eventState)) {
      HelpEventRouteEffect.Ignore -> Unit
      is HelpEventRouteEffect.ShowMessage -> {
        snackbarHostState.showSnackbar(message = action.message)
      }
      is HelpEventRouteEffect.ShowUpdateAvailable -> {
        val result =
          snackbarHostState.showSnackbar(
            message = updateAvailableText,
            actionLabel = openActionText,
            duration = SnackbarDuration.Long,
          )

        when (helpUpdateAvailableSnackbarAction(result.toSnackbarActionResult())) {
          HelpUpdateAvailableSnackbarAction.OpenReleases -> currentOnOpenLink(action.releasesUrl)
          HelpUpdateAvailableSnackbarAction.Ignore -> Unit
        }
      }
    }

    eventState = helpConsumedEventState()
  }

  HelpContent(
    modifier = modifier,
    uiState = uiState,
    tipsText = tipsText,
    appName = appName,
    appIconPainter = appIconPainter,
    mihomoWikiUrl = mihomoWikiUrl,
    mihomoCoreUrl = mihomoCoreUrl,
    tabbyUrl = tabbyUrl,
    snackbarHostState = snackbarHostState,
    onOpenLink = onOpenLink,
    onCopyVersion = onCopyVersion,
    onCheckForUpdates = {
      when (helpUpdateCheckRequestAction(uiState)) {
        HelpUpdateCheckRequestAction.StartCheck -> Unit
        HelpUpdateCheckRequestAction.Ignore -> return@HelpContent
      }

      scope.launch {
        uiState = uiState.withUpdateCheckStarted()
        try {
          eventState =
            helpUpdateCheckEventState(
              fetchLatestReleaseTag = currentOnFetchLatestReleaseTag,
              loadLocalVersion = currentOnLoadLocalVersion,
              releasesUrl = releasesUrl,
              alreadyUpToDateMessage = alreadyUpToDateMessage,
              updateCheckFailedMessage = updateCheckFailedMessage,
            )
        } catch (e: Exception) {
          currentOnUpdateCheckFailure(e)
          eventState = helpUpdateCheckFailureEventState(updateCheckFailedMessage)
        } finally {
          uiState = uiState.withUpdateCheckFinished()
        }
      }
    },
  )
}

private const val DEFAULT_HELP_TIPS_TEXT = "Tabby is freeware and does not provide a proxy service."
