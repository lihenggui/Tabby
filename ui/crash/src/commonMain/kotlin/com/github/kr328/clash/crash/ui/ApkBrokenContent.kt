package com.github.kr328.clash.crash.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.OutlineInfo
import com.github.kr328.clash.ui.icon.TabbyIcons
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory
import org.jetbrains.compose.resources.stringResource
import tabby.ui.crash.generated.resources.Res as CrashRes
import tabby.ui.crash.generated.resources.application_broken
import tabby.ui.crash.generated.resources.application_broken_tips
import tabby.ui.crash.generated.resources.github_releases
import tabby.ui.crash.generated.resources.reinstall

@Composable
internal fun ApkBrokenContent(
  releasesUrl: String,
  onOpenReleases: () -> Unit,
  modifier: Modifier = Modifier,
) {
  TabbyScaffold(
    modifier = modifier,
    title = stringResource(CrashRes.string.application_broken),
  ) { innerPadding ->
    ProvidePreferenceLocals {
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        preference(
          key = "tips",
          title = {},
          summary = { Text(stringResource(CrashRes.string.application_broken_tips)) },
          icon = { Icon(imageVector = TabbyIcons.OutlineInfo, contentDescription = null) },
        )
        preferenceCategory(
          key = "cat_reinstall",
          title = { Text(stringResource(CrashRes.string.reinstall)) },
        )
        preference(
          key = "github_releases",
          title = { Text(stringResource(CrashRes.string.github_releases)) },
          summary = { Text(releasesUrl) },
          onClick = onOpenReleases,
        )
      }
    }
  }
}
