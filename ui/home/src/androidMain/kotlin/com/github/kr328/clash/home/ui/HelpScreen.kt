package com.github.kr328.clash.home.ui

import android.content.ClipData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource as androidStringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.glue.util.openLink
import com.github.kr328.clash.home.MIHOMO_CORE
import com.github.kr328.clash.home.MIHOMO_WIKI
import com.github.kr328.clash.home.R
import com.github.kr328.clash.home.TABBY_GITHUB
import com.github.kr328.clash.home.vm.HelpViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tabby.ui.home.generated.resources.Res as HomeRes
import tabby.ui.home.generated.resources.open
import tabby.ui.home.generated.resources.update_available
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.copied

@Composable
internal fun HelpScreen(modifier: Modifier = Modifier, viewModel: HelpViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val eventState by viewModel.eventState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val context = LocalContext.current
  val clipboard = LocalClipboard.current
  val scope = rememberCoroutineScope()
  val updateAvailableText = stringResource(HomeRes.string.update_available)
  val openActionText = stringResource(HomeRes.string.open)
  val messageCopied = stringResource(SharedRes.string.copied)

  LaunchedEffect(eventState) {
    when (val action = helpEventPlatformAction(eventState)) {
      HelpEventPlatformAction.Ignore -> Unit
      is HelpEventPlatformAction.ShowMessage -> {
        snackbarHostState.showSnackbar(message = action.message)
      }
      is HelpEventPlatformAction.ShowUpdateAvailable -> {
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
    viewModel.consumeEvent()
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
        val clipEntry = ClipData.newPlainText("version", version).toClipEntry()
        clipboard.setClipEntry(clipEntry)
        snackbarHostState.showSnackbar(message = messageCopied, withDismissAction = true)
      }
    },
    onCheckForUpdates = viewModel::checkForUpdates,
  )
}
