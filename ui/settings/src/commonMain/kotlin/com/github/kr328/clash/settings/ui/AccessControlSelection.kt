package com.github.kr328.clash.settings.ui

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
