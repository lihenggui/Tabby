package com.github.kr328.clash.crash.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.github.kr328.clash.glue.util.openLink

@Composable
internal fun ApkBrokenScreen() {
  val context = LocalContext.current
  ApkBrokenRouteContent(onOpenReleases = context::openLink)
}
