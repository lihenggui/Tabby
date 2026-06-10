package com.github.kr328.clash.proxy

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.proxy.ui.ProxyScreen

fun EntryProviderScope<NavKey>.proxyEntries(onReLaunch: () -> Unit) {
  proxyEntries(proxyContent = { ProxyScreen(onReLaunch = onReLaunch) })
}
