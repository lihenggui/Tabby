package com.github.kr328.clash.settings.ui

import com.github.kr328.clash.core.model.AccessControlSort

internal data class AccessControlUiState<T>(
  val apps: List<T>,
  val settings: AccessControlSettingsState,
)

data class AccessControlClipboardImportPayload(
  val hasPrimaryClipItem: Boolean,
  val clipboardText: String?,
)

internal data class AccessControlExportPayload(
  val label: String,
  val text: String,
)

internal sealed interface AccessControlClipboardImportAction {
  data class Import(val clipboardText: String?) : AccessControlClipboardImportAction

  data object Ignore : AccessControlClipboardImportAction
}

internal fun <T> accessControlInitialUiState(
  selected: Set<String> = emptySet(),
  sort: AccessControlSort,
  reverse: Boolean,
  showSystemApps: Boolean,
  apps: List<T> = emptyList(),
): AccessControlUiState<T> {
  return AccessControlUiState(
    apps = apps,
    settings =
      AccessControlSettingsState(
        selected = selected,
        sort = sort,
        reverse = reverse,
        showSystemApps = showSystemApps,
      ),
  )
}

internal fun accessControlClipboardHasPrimaryClipItem(primaryClipItemCount: Int?): Boolean {
  return primaryClipItemCount != null && primaryClipItemCount > 0
}

internal fun accessControlClipboardImportPayload(
  hasPrimaryClipItem: Boolean,
  clipboardText: String?,
): AccessControlClipboardImportPayload {
  return AccessControlClipboardImportPayload(
    hasPrimaryClipItem = hasPrimaryClipItem,
    clipboardText = if (hasPrimaryClipItem) clipboardText else null,
  )
}

internal fun accessControlClipboardImportAction(
  payload: AccessControlClipboardImportPayload
): AccessControlClipboardImportAction {
  return if (payload.hasPrimaryClipItem) {
    AccessControlClipboardImportAction.Import(payload.clipboardText)
  } else {
    AccessControlClipboardImportAction.Ignore
  }
}

internal fun accessControlPersistRequestedFromStopEvent(isStopEvent: Boolean): Boolean {
  return isStopEvent
}

internal fun <T> AccessControlUiState<T>.withAccessControlApps(
  apps: List<T>
): AccessControlUiState<T> {
  return copy(apps = apps)
}

internal fun <T> AccessControlUiState<T>.withAccessControlSelectedPackages(
  selected: Set<String>
): AccessControlUiState<T> {
  return copy(settings = updateAccessControlSelectedPackages(settings, selected))
}

internal fun <T> AccessControlUiState<T>.withToggledAccessControlPackage(
  packageName: String
): AccessControlUiState<T> {
  return copy(settings = toggleAccessControlSelectedPackage(settings, packageName))
}

internal fun <T> AccessControlUiState<T>.withAllAccessControlPackages(
  packageNames: Iterable<String>
): AccessControlUiState<T> {
  return copy(settings = selectAllAccessControlPackages(settings, packageNames))
}

internal fun <T> AccessControlUiState<T>.withAllVisibleAccessControlPackages(
  packageName: (T) -> String
): AccessControlUiState<T> {
  return withAllAccessControlPackages(apps.map(packageName))
}

internal fun <T> AccessControlUiState<T>.withNoAccessControlPackages(): AccessControlUiState<T> {
  return copy(settings = selectNoAccessControlPackages(settings))
}

internal fun <T> AccessControlUiState<T>.withInvertedAccessControlPackages(
  packageNames: Iterable<String>
): AccessControlUiState<T> {
  return copy(settings = invertAccessControlPackages(settings, packageNames))
}

internal fun <T> AccessControlUiState<T>.withInvertedVisibleAccessControlPackages(
  packageName: (T) -> String
): AccessControlUiState<T> {
  return withInvertedAccessControlPackages(apps.map(packageName))
}

internal fun <T> AccessControlUiState<T>.withImportedAccessControlPackages(
  clipboardText: String?,
  installedPackageNames: Iterable<String>,
): AccessControlUiState<T> {
  return copy(
    settings = importAccessControlPackages(settings, clipboardText, installedPackageNames)
  )
}

internal fun <T> AccessControlUiState<T>.withImportedVisibleAccessControlPackages(
  clipboardText: String?,
  packageName: (T) -> String,
): AccessControlUiState<T> {
  return withImportedAccessControlPackages(
    clipboardText = clipboardText,
    installedPackageNames = apps.map(packageName),
  )
}

internal fun <T> accessControlImportClipboardState(
  state: AccessControlUiState<T>,
  clipboardText: String?,
  packageName: (T) -> String,
): AccessControlUiState<T> {
  return state.withImportedVisibleAccessControlPackages(
    clipboardText = clipboardText,
    packageName = packageName,
  )
}

internal fun <T> accessControlExportClipboardText(state: AccessControlUiState<T>): String {
  return exportAccessControlPackages(state.settings.selected)
}

internal fun <T> accessControlExportPayload(
  state: AccessControlUiState<T>
): AccessControlExportPayload {
  return accessControlExportPayload(exportAccessControlPackages(state.settings.selected))
}

internal fun accessControlExportPayload(text: String): AccessControlExportPayload {
  return AccessControlExportPayload(
    label = "packages",
    text = text,
  )
}

internal fun <T> AccessControlUiState<T>.withAccessControlSort(
  sort: AccessControlSort
): AccessControlUiState<T> {
  return copy(settings = updateAccessControlSort(settings, sort))
}

internal fun <T> AccessControlUiState<T>.withAccessControlReverse(
  reverse: Boolean
): AccessControlUiState<T> {
  return copy(settings = updateAccessControlReverse(settings, reverse))
}

internal fun <T> AccessControlUiState<T>.withAccessControlShowSystemApps(
  showSystemApps: Boolean
): AccessControlUiState<T> {
  return copy(settings = updateAccessControlShowSystemApps(settings, showSystemApps))
}
