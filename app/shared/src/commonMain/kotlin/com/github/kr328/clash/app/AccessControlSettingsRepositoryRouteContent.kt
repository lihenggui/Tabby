package com.github.kr328.clash.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.AccessControlSort
import com.github.kr328.clash.settings.ui.AccessControlPackage
import com.github.kr328.clash.settings.ui.AccessControlRouteContent
import com.github.kr328.clash.settingsstore.TabbyAccessControlSettings
import com.github.kr328.clash.settingsstore.TabbyAccessControlSettingsRepository

@Composable
internal fun AccessControlSettingsRepositoryRouteContent(
  repository: TabbyAccessControlSettingsRepository,
  modifier: Modifier = Modifier,
  initialApps: List<AccessControlPackage> = emptyList(),
  defaults: TabbyAccessControlSettings = TabbyAccessControlSettings(),
  onSelectedPackagesChange: (Set<String>) -> Unit = {},
  onSortChange: (AccessControlSort) -> Unit = {},
  onReverseChange: (Boolean) -> Unit = {},
  onShowSystemAppsChange: (Boolean) -> Unit = {},
  onImportClipboardText: () -> String? = { null },
  onExportClipboardText: (String) -> Unit = {},
) {
  val settings = remember(repository, defaults) { repository.query(defaults) }

  AccessControlRouteContent(
    modifier = modifier,
    initialApps = initialApps,
    initialSelected = settings.selectedPackages,
    initialSort = settings.sort,
    initialReverse = settings.reverse,
    initialShowSystemApps = settings.showSystemApps,
    onSelectedChange = { value ->
      repository.setSelectedPackages(value)
      onSelectedPackagesChange(value)
    },
    onSortChange = { value ->
      repository.setSort(value)
      onSortChange(value)
    },
    onReverseChange = { value ->
      repository.setReverse(value)
      onReverseChange(value)
    },
    onShowSystemAppsChange = { value ->
      repository.setShowSystemApps(value)
      onShowSystemAppsChange(value)
    },
    onImportClipboardText = onImportClipboardText,
    onExportClipboardText = onExportClipboardText,
  )
}
