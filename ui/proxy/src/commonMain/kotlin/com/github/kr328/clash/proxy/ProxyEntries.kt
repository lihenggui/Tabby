package com.github.kr328.clash.proxy

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.proxyEntries(proxyContent: @Composable () -> Unit) {
  entry<ProxyRoute.Proxy> { proxyContent() }
}
