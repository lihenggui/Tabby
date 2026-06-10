package com.github.kr328.clash.crash.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.kr328.clash.crash.TABBY_GITHUB

@Composable
fun ApkBrokenRouteContent(
  onOpenReleases: (String) -> Unit,
  modifier: Modifier = Modifier,
  releasesUrl: String = TABBY_GITHUB,
) {
  ApkBrokenContent(
    modifier = modifier,
    releasesUrl = releasesUrl,
    onOpenReleases = onOpenReleases,
  )
}
