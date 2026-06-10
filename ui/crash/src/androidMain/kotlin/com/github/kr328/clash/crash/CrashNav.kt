package com.github.kr328.clash.crash

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.crash.ui.ApkBrokenScreen
import com.github.kr328.clash.crash.ui.AppCrashedScreen

fun EntryProviderScope<NavKey>.crashEntries() {
  crashEntries(
    apkBrokenContent = { ApkBrokenScreen() },
    appCrashedContent = { AppCrashedScreen() },
  )
}
