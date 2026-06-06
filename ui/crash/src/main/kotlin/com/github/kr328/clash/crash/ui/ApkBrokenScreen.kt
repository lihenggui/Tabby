package com.github.kr328.clash.crash.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.github.kr328.clash.glue.util.TABBY_GITHUB
import com.github.kr328.clash.glue.util.openLink

@Composable
internal fun ApkBrokenScreen() {
  val context = LocalContext.current
  ApkBrokenContent(releasesUrl = TABBY_GITHUB, onOpenReleases = { context.openLink(TABBY_GITHUB) })
}
