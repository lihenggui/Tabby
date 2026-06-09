package com.github.kr328.clash.home.ui

import android.content.ClipData
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.painterResource
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.di.AppInfoProvider.Companion.instance as appInfoProvider
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.bridge.Bridge
import com.github.kr328.clash.glue.util.openLink
import com.github.kr328.clash.home.TABBY_REPO
import com.github.kr328.clash.network.GitHubReleaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun HelpScreen(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val appContext = context.applicationContext
  val clipboard = LocalClipboard.current
  val releaseClient = remember { GitHubReleaseClient() }

  HelpRouteContent(
    modifier = modifier,
    appIconPainter = painterResource(CommonR.drawable.ic_tabby_small),
    onOpenLink = { url -> context.openLink(url) },
    onCopyVersion = { label, text ->
      val clipEntry = ClipData.newPlainText(label, text).toClipEntry()
      clipboard.setClipEntry(clipEntry)
    },
    onLoadVersionInfo = { withContext(Dispatchers.IO) { appContext.loadVersionInfo() } },
    onFetchLatestReleaseTag = { releaseClient.fetchLatestReleaseTag(TABBY_REPO) },
    onLoadLocalVersion = { withContext(Dispatchers.IO) { appContext.loadPackageVersionName() } },
    onUpdateCheckFailure = { e -> Log.e("Check for updates failed: ${e.message}", e) },
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
