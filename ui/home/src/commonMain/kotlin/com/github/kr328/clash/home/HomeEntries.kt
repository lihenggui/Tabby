package com.github.kr328.clash.home

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.homeEntries(
  homeContent: @Composable () -> Unit,
  helpContent: @Composable () -> Unit,
) {
  entry<HomeRoute.Home> { homeContent() }
  entry<HomeRoute.Help> { helpContent() }
}
