package com.github.kr328.clash.home.ui

import android.content.ClipData
import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource as androidStringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.di.AppInfoProvider.Companion.instance as appInfoProvider
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.bridge.Bridge
import com.github.kr328.clash.glue.util.openLink
import com.github.kr328.clash.home.MIHOMO_CORE
import com.github.kr328.clash.home.MIHOMO_WIKI
import com.github.kr328.clash.home.R
import com.github.kr328.clash.home.TABBY_GITHUB
import com.github.kr328.clash.home.TABBY_RELEASES_LATEST
import com.github.kr328.clash.home.TABBY_REPO
import com.github.kr328.clash.network.GitHubReleaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import tabby.ui.home.generated.resources.Res as HomeRes
import tabby.ui.home.generated.resources.open
import tabby.ui.home.generated.resources.update_available
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.copied

@Composable
internal fun HelpScreen(modifier: Modifier = Modifier) {
  var uiState by remember { mutableStateOf(helpInitialContentState()) }
  var eventState by remember { mutableStateOf<HelpEventState>(helpInitialEventState()) }
  val snackbarHostState = remember { SnackbarHostState() }
  val context = LocalContext.current
  val appContext = context.applicationContext
  val clipboard = LocalClipboard.current
  val scope = rememberCoroutineScope()
  val releaseClient = remember { GitHubReleaseClient() }
  val updateAvailableText = stringResource(HomeRes.string.update_available)
  val openActionText = stringResource(HomeRes.string.open)
  val messageCopied = stringResource(SharedRes.string.copied)

  LaunchedEffect(appContext) {
    val versionInfo = withContext(Dispatchers.IO) { appContext.loadVersionInfo() }

    uiState = uiState.withVersionInfo(versionInfo)
  }

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
          HelpUpdateAvailableSnackbarAction.OpenReleases -> context.openLink(action.releasesUrl)
          HelpUpdateAvailableSnackbarAction.Ignore -> Unit
        }
      }
    }
    eventState = helpConsumedEventState()
  }

  HelpRouteContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    checkingForUpdates = uiState.checkingForUpdates,
    appVersion = uiState.appVersion,
    coreVersion = uiState.coreVersion,
    tipsText = AnnotatedString.fromHtml(androidStringResource(R.string.tips_help)),
    appName = androidStringResource(CommonR.string.tabby),
    appIconPainter = painterResource(CommonR.drawable.ic_tabby_small),
    mihomoWikiUrl = MIHOMO_WIKI,
    mihomoCoreUrl = MIHOMO_CORE,
    tabbyUrl = TABBY_GITHUB,
    onOpenLink = { url -> context.openLink(url) },
    onCopyVersion = { version ->
      scope.launch {
        val payload = helpCopyVersionPayload(version)
        val clipEntry = ClipData.newPlainText(payload.label, payload.text).toClipEntry()
        clipboard.setClipEntry(clipEntry)
        snackbarHostState.showSnackbar(message = messageCopied, withDismissAction = true)
      }
    },
    onCheckForUpdates = {
      when (helpUpdateCheckRequestAction(uiState)) {
        HelpUpdateCheckRequestAction.StartCheck -> Unit
        HelpUpdateCheckRequestAction.Ignore -> return@HelpRouteContent
      }

      scope.launch {
        uiState = uiState.withUpdateCheckStarted()
        try {
          val latestTag = releaseClient.fetchLatestReleaseTag(TABBY_REPO)
          val localVersion =
            when (helpLocalVersionLoadAction(latestTag)) {
              HelpLocalVersionLoadAction.Load ->
                withContext(Dispatchers.IO) { appContext.loadPackageVersionName() }
              HelpLocalVersionLoadAction.Ignore -> null
            }
          val action = helpUpdateCheckAction(latestTag = latestTag, localVersion = localVersion)

          eventState =
            helpUpdateCheckEventState(
              action = action,
              releasesUrl = TABBY_RELEASES_LATEST,
              alreadyUpToDateMessage = appContext.getString(R.string.already_up_to_date),
              updateCheckFailedMessage = appContext.getString(R.string.check_update_failed),
            )
        } catch (e: Exception) {
          Log.e("Check for updates failed: ${e.message}", e)
          eventState =
            helpUpdateCheckFailureEventState(appContext.getString(R.string.check_update_failed))
        } finally {
          uiState = uiState.withUpdateCheckFinished()
        }
      }
    },
  )
}

private fun Context.loadVersionInfo(): HelpVersionInfo {
  return HelpVersionInfo(
    appVersion =
      formatAppVersionInfo(
        versionName = loadPackageVersionName(),
        buildCommit = appInfoProvider.buildCommit,
      ),
    coreVersion = Bridge.nativeCoreVersion(),
  )
}

private fun Context.loadPackageVersionName(): String? {
  return packageManager.getPackageInfo(packageName, 0).versionName
}
