package com.github.kr328.clash.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.AccessControlSort

data class AccessControlPackage(
  val packageName: String,
  val label: String,
  val installTime: Long = 0,
  val updateDate: Long = 0,
)

@Composable
fun AccessControlRouteContent(
  modifier: Modifier = Modifier,
  initialApps: List<AccessControlPackage> = emptyList(),
  initialSelected: Set<String> = emptySet(),
  initialSort: AccessControlSort = AccessControlSort.Label,
  initialReverse: Boolean = false,
  initialShowSystemApps: Boolean = false,
  onSelectedChange: (Set<String>) -> Unit = {},
  onSortChange: (AccessControlSort) -> Unit = {},
  onReverseChange: (Boolean) -> Unit = {},
  onShowSystemAppsChange: (Boolean) -> Unit = {},
  onImportClipboardPayload: () -> AccessControlClipboardImportPayload = {
    AccessControlClipboardImportPayload(hasPrimaryClipItem = false, clipboardText = null)
  },
  onExportClipboardPayload: (AccessControlExportPayload) -> Unit = {},
  appIcon: @Composable (AccessControlPackage) -> Unit = {},
) {
  var uiState by remember {
    mutableStateOf(
      accessControlPackageUiState(
        apps = initialApps,
        selected = initialSelected,
        sort = initialSort,
        reverse = initialReverse,
        showSystemApps = initialShowSystemApps,
      )
    )
  }

  LaunchedEffect(
    initialApps,
    initialSelected,
    initialSort,
    initialReverse,
    initialShowSystemApps,
  ) {
    uiState =
      accessControlPackageUiState(
        apps = initialApps,
        selected = initialSelected,
        sort = initialSort,
        reverse = initialReverse,
        showSystemApps = initialShowSystemApps,
      )
  }

  AccessControlContent(
    apps = uiState.apps,
    selected = uiState.settings.selected,
    sort = uiState.settings.sort,
    reverse = uiState.settings.reverse,
    showSystemApps = uiState.settings.showSystemApps,
    actions =
      object : AccessControlActions {
        override fun toggleApp(packageName: String) {
          uiState = uiState.withToggledAccessControlPackage(packageName).withSortedPackages()
          onSelectedChange(uiState.settings.selected)
        }

        override fun selectAll() {
          uiState =
            uiState
              .withAllVisibleAccessControlPackages(AccessControlPackage::packageName)
              .withSortedPackages()
          onSelectedChange(uiState.settings.selected)
        }

        override fun selectNone() {
          uiState = uiState.withNoAccessControlPackages().withSortedPackages()
          onSelectedChange(uiState.settings.selected)
        }

        override fun selectInvert() {
          uiState =
            uiState
              .withInvertedVisibleAccessControlPackages(AccessControlPackage::packageName)
              .withSortedPackages()
          onSelectedChange(uiState.settings.selected)
        }

        override fun importFromClipboard() {
          when (val action = accessControlClipboardImportAction(onImportClipboardPayload())) {
            is AccessControlClipboardImportAction.Import -> {
              uiState =
                accessControlImportClipboardState(
                    state = uiState,
                    clipboardText = action.clipboardText,
                    packageName = AccessControlPackage::packageName,
                  )
                  .withSortedPackages()
              onSelectedChange(uiState.settings.selected)
            }
            AccessControlClipboardImportAction.Ignore -> Unit
          }
        }

        override fun exportToClipboard() {
          onExportClipboardPayload(accessControlExportPayload(uiState))
        }

        override fun updateSort(sort: AccessControlSort) {
          uiState = uiState.withAccessControlSort(sort).withSortedPackages()
          onSortChange(sort)
        }

        override fun updateReverse(reverse: Boolean) {
          uiState = uiState.withAccessControlReverse(reverse).withSortedPackages()
          onReverseChange(reverse)
        }

        override fun updateShowSystemApps(show: Boolean) {
          uiState = uiState.withAccessControlShowSystemApps(show)
          onShowSystemAppsChange(show)
        }
      },
    appPackageName = AccessControlPackage::packageName,
    appLabel = AccessControlPackage::label,
    modifier = modifier,
    appIcon = appIcon,
  )
}

private fun accessControlPackageUiState(
  apps: List<AccessControlPackage>,
  selected: Set<String>,
  sort: AccessControlSort,
  reverse: Boolean,
  showSystemApps: Boolean,
): AccessControlUiState<AccessControlPackage> {
  return accessControlInitialUiState(
      apps = apps,
      selected = selected,
      sort = sort,
      reverse = reverse,
      showSystemApps = showSystemApps,
    )
    .withSortedPackages()
}

private fun AccessControlUiState<AccessControlPackage>.withSortedPackages() =
  withAccessControlApps(
    sortAccessControlApps(
      apps = apps,
      selectedPackageNames = settings.selected,
      sort = settings.sort,
      reverse = settings.reverse,
      packageName = AccessControlPackage::packageName,
      label = AccessControlPackage::label,
      installTime = AccessControlPackage::installTime,
      updateTime = AccessControlPackage::updateDate,
    )
  )
