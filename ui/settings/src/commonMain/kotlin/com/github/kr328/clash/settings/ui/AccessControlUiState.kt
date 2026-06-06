package com.github.kr328.clash.settings.ui

import com.github.kr328.clash.core.model.AccessControlSort

internal data class AccessControlUiState<T>(
  val apps: List<T>,
  val settings: AccessControlSettingsState,
)

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

internal fun <T> AccessControlUiState<T>.withNoAccessControlPackages(): AccessControlUiState<T> {
  return copy(settings = selectNoAccessControlPackages(settings))
}

internal fun <T> AccessControlUiState<T>.withInvertedAccessControlPackages(
  packageNames: Iterable<String>
): AccessControlUiState<T> {
  return copy(settings = invertAccessControlPackages(settings, packageNames))
}

internal fun <T> AccessControlUiState<T>.withImportedAccessControlPackages(
  clipboardText: String?,
  installedPackageNames: Iterable<String>,
): AccessControlUiState<T> {
  return copy(
    settings = importAccessControlPackages(settings, clipboardText, installedPackageNames)
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
