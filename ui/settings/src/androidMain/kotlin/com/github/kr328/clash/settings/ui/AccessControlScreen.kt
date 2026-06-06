package com.github.kr328.clash.settings.ui

import android.widget.ImageView
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.glue.model.AppInfo
import com.github.kr328.clash.settings.vm.AccessControlViewModel
import com.github.kr328.clash.ui.lifecycle.viewModelWithLifecycle
import com.github.kr328.clash.ui.theme.tabbyDimens

@Composable
internal fun AccessControlScreen(
  modifier: Modifier = Modifier,
  viewModel: AccessControlViewModel = viewModelWithLifecycle(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  AccessControlContent(
    apps = uiState.apps,
    selected = uiState.settings.selected,
    sort = uiState.settings.sort,
    reverse = uiState.settings.reverse,
    showSystemApps = uiState.settings.showSystemApps,
    actions = viewModel,
    appPackageName = AppInfo::packageName,
    appLabel = AppInfo::label,
    modifier = modifier,
    appIcon = { app -> AccessControlAppIcon(app) },
  )
}

@Composable
private fun AccessControlAppIcon(app: AppInfo) {
  val dimens = tabbyDimens

  AndroidView(
    factory = { context ->
      ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
    },
    update = { it.setImageDrawable(app.icon) },
    modifier = Modifier.size(dimens.itemHeaderComponentSize),
  )
}
