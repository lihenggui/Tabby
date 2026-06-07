package com.github.kr328.clash.settings.ui

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
  val apps = remember(uiState.apps) { uiState.apps.map(AppInfo::toAccessControlPackage) }
  val icons = remember(uiState.apps) { uiState.apps.associate { it.packageName to it.icon } }

  AccessControlRouteContent(
    modifier = modifier,
    initialApps = apps,
    initialSelected = uiState.settings.selected,
    initialSort = uiState.settings.sort,
    initialReverse = uiState.settings.reverse,
    initialShowSystemApps = uiState.settings.showSystemApps,
    onSelectedChange = viewModel::setSelectedPackages,
    onSortChange = viewModel::setSort,
    onReverseChange = viewModel::setReverse,
    onShowSystemAppsChange = viewModel::setShowSystemApps,
    onImportClipboardPayload = viewModel::importClipboardPayload,
    onExportClipboardText = viewModel::exportClipboardText,
    appIcon = { app -> icons[app.packageName]?.let { icon -> AccessControlAppIcon(icon) } },
  )
}

@Composable
private fun AccessControlAppIcon(icon: Drawable) {
  val dimens = tabbyDimens

  AndroidView(
    factory = { context ->
      ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
    },
    update = { it.setImageDrawable(icon) },
    modifier = Modifier.size(dimens.itemHeaderComponentSize),
  )
}

private fun AppInfo.toAccessControlPackage(): AccessControlPackage {
  return AccessControlPackage(
    packageName = packageName,
    label = label,
    installTime = installTime,
    updateDate = updateDate,
  )
}
