package com.github.kr328.clash.crash.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ApkBrokenRouteContent(
  releasesUrl: String,
  onOpenReleases: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ApkBrokenContent(
    modifier = modifier,
    releasesUrl = releasesUrl,
    onOpenReleases = onOpenReleases,
  )
}
