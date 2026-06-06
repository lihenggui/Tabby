package com.github.kr328.clash.crash

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.crashEntries(
  apkBrokenContent: @Composable () -> Unit,
  appCrashedContent: @Composable () -> Unit,
) {
  entry<CrashRoute.ApkBroken> { apkBrokenContent() }
  entry<CrashRoute.AppCrashed> { appCrashedContent() }
}
