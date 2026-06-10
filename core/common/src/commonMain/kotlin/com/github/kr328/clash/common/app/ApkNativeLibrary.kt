package com.github.kr328.clash.common.app

const val TABBY_CLASH_NATIVE_LIBRARY_NAME = "libclash.so"

fun tabbyApkHasSupportedClashNativeLibrary(
  supportedAbis: Set<String>,
  apkEntryNames: Sequence<String>,
): Boolean {
  return apkEntryNames.mapNotNull { it.tabbyClashNativeLibraryAbi() }.any { it in supportedAbis }
}

private fun String.tabbyClashNativeLibraryAbi(): String? {
  val prefix = "lib/"
  val suffix = "/$TABBY_CLASH_NATIVE_LIBRARY_NAME"

  if (!startsWith(prefix) || !endsWith(suffix)) return null

  return removePrefix(prefix).removeSuffix(suffix).takeIf { it.isNotEmpty() && '/' !in it }
}
