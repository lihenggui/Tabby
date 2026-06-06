package com.github.kr328.clash.settings.ui

import com.github.kr328.clash.core.model.AccessControlSort

internal data class AccessControlSettingsState(
  val selected: Set<String>,
  val sort: AccessControlSort,
  val reverse: Boolean,
  val showSystemApps: Boolean,
)

internal fun updateAccessControlSelectedPackages(
  state: AccessControlSettingsState,
  selected: Set<String>,
): AccessControlSettingsState {
  return state.copy(selected = selected)
}

internal fun toggleAccessControlSelectedPackage(
  state: AccessControlSettingsState,
  packageName: String,
): AccessControlSettingsState {
  return state.copy(selected = toggleAccessControlPackage(state.selected, packageName))
}

internal fun selectAllAccessControlPackages(
  state: AccessControlSettingsState,
  packageNames: Iterable<String>,
): AccessControlSettingsState {
  return state.copy(selected = selectAllAccessControlPackages(packageNames))
}

internal fun selectNoAccessControlPackages(
  state: AccessControlSettingsState
): AccessControlSettingsState {
  return state.copy(selected = emptySet())
}

internal fun invertAccessControlPackages(
  state: AccessControlSettingsState,
  packageNames: Iterable<String>,
): AccessControlSettingsState {
  return state.copy(
    selected =
      invertAccessControlPackages(
        selected = state.selected,
        packageNames = packageNames,
      )
  )
}

internal fun importAccessControlPackages(
  state: AccessControlSettingsState,
  clipboardText: String?,
  installedPackageNames: Iterable<String>,
): AccessControlSettingsState {
  return state.copy(
    selected =
      importAccessControlPackages(
        clipboardText = clipboardText,
        installedPackageNames = installedPackageNames,
      )
  )
}

internal fun updateAccessControlSort(
  state: AccessControlSettingsState,
  sort: AccessControlSort,
): AccessControlSettingsState {
  return state.copy(sort = sort)
}

internal fun updateAccessControlReverse(
  state: AccessControlSettingsState,
  reverse: Boolean,
): AccessControlSettingsState {
  return state.copy(reverse = reverse)
}

internal fun updateAccessControlShowSystemApps(
  state: AccessControlSettingsState,
  showSystemApps: Boolean,
): AccessControlSettingsState {
  return state.copy(showSystemApps = showSystemApps)
}

internal fun toggleAccessControlPackage(
  selected: Set<String>,
  packageName: String,
): Set<String> {
  return if (packageName in selected) selected - packageName else selected + packageName
}

internal fun selectAllAccessControlPackages(packageNames: Iterable<String>): Set<String> {
  return packageNames.toSet()
}

internal fun invertAccessControlPackages(
  selected: Set<String>,
  packageNames: Iterable<String>,
): Set<String> {
  return packageNames.toSet() - selected
}

internal fun importAccessControlPackages(
  clipboardText: String?,
  installedPackageNames: Iterable<String>,
): Set<String> {
  val imported =
    clipboardText?.lineSequence()?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet().orEmpty()

  return installedPackageNames.toSet().intersect(imported)
}

internal fun exportAccessControlPackages(selected: Set<String>): String {
  return selected.sorted().joinToString("\n")
}

internal fun <T> filterAccessControlApps(
  apps: Iterable<T>,
  keyword: String,
  label: (T) -> String,
  packageName: (T) -> String,
): List<T> {
  if (keyword.isBlank()) return emptyList()

  return apps.filter {
    label(it).contains(keyword, ignoreCase = true) ||
      packageName(it).contains(keyword, ignoreCase = true)
  }
}

internal fun <T> sortAccessControlApps(
  apps: Iterable<T>,
  selectedPackageNames: Set<String>,
  sort: AccessControlSort,
  reverse: Boolean,
  packageName: (T) -> String,
  label: (T) -> String,
  installTime: (T) -> Long,
  updateTime: (T) -> Long,
): List<T> {
  val selectedFirst = compareByDescending<T> { packageName(it) in selectedPackageNames }
  val sortComparator =
    when (sort) {
      AccessControlSort.Label -> compareBy(label)
      AccessControlSort.PackageName -> compareBy(packageName)
      AccessControlSort.InstallTime -> compareBy(installTime)
      AccessControlSort.UpdateTime -> compareBy(updateTime)
    }
  val comparator =
    if (reverse) selectedFirst.thenDescending(sortComparator)
    else selectedFirst.then(sortComparator)

  return apps.sortedWith(comparator)
}
