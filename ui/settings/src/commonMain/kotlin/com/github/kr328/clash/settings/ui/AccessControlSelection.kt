package com.github.kr328.clash.settings.ui

import com.github.kr328.clash.core.model.AccessControlSort

internal data class AccessControlSettingsState(
  val selected: Set<String>,
  val sort: AccessControlSort,
  val reverse: Boolean,
  val showSystemApps: Boolean,
)

internal data class AccessControlReloadRequest(
  val selected: Set<String>,
  val sort: AccessControlSort,
  val reverse: Boolean,
  val showSystemApps: Boolean,
)

internal data class AccessControlPersistPlan(
  val shouldPersistSelection: Boolean,
  val shouldRestartService: Boolean,
)

internal fun accessControlReloadRequest(
  selected: Set<String>,
  sort: AccessControlSort,
  reverse: Boolean,
  showSystemApps: Boolean,
): AccessControlReloadRequest {
  return AccessControlReloadRequest(
    selected = selected,
    sort = sort,
    reverse = reverse,
    showSystemApps = showSystemApps,
  )
}

internal fun planAccessControlPersist(
  selected: Set<String>,
  persistedSelection: Set<String>,
  clashRunning: Boolean,
): AccessControlPersistPlan {
  val selectionChanged = selected != persistedSelection
  return AccessControlPersistPlan(
    shouldPersistSelection = selectionChanged,
    shouldRestartService = selectionChanged && clashRunning,
  )
}

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

internal fun accessControlSystemAppFromPlatformFlags(
  flags: Int?,
  systemAppFlag: Int,
): Boolean {
  return flags?.let { it and systemAppFlag != 0 } == true
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

internal fun <T> filterAccessControlPackageCandidates(
  packages: Iterable<T>,
  currentPackageName: String,
  showSystemApps: Boolean,
  packageName: (T) -> String,
  hasAppMetadata: (T) -> Boolean,
  hasInternetPermission: (T) -> Boolean,
  hasSystemUid: (T) -> Boolean,
  isSystemApp: (T) -> Boolean,
): List<T> {
  return packages.filter {
    packageName(it) != currentPackageName &&
      hasAppMetadata(it) &&
      (hasInternetPermission(it) || hasSystemUid(it)) &&
      (showSystemApps || !isSystemApp(it))
  }
}

internal fun <P, A> loadAccessControlApps(
  packages: Iterable<P>,
  selectedPackageNames: Set<String>,
  sort: AccessControlSort,
  reverse: Boolean,
  currentPackageName: String,
  showSystemApps: Boolean,
  packageName: (P) -> String,
  hasAppMetadata: (P) -> Boolean,
  hasInternetPermission: (P) -> Boolean,
  hasSystemUid: (P) -> Boolean,
  isSystemApp: (P) -> Boolean,
  toApp: (P) -> A,
  appPackageName: (A) -> String,
  appLabel: (A) -> String,
  appInstallTime: (A) -> Long,
  appUpdateTime: (A) -> Long,
): List<A> {
  val apps =
    filterAccessControlPackageCandidates(
        packages = packages,
        currentPackageName = currentPackageName,
        showSystemApps = showSystemApps,
        packageName = packageName,
        hasAppMetadata = hasAppMetadata,
        hasInternetPermission = hasInternetPermission,
        hasSystemUid = hasSystemUid,
        isSystemApp = isSystemApp,
      )
      .map(toApp)

  return sortAccessControlApps(
    apps = apps,
    selectedPackageNames = selectedPackageNames,
    sort = sort,
    reverse = reverse,
    packageName = appPackageName,
    label = appLabel,
    installTime = appInstallTime,
    updateTime = appUpdateTime,
  )
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
