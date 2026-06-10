package com.github.kr328.clash.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.core.model.DarkMode
import com.github.kr328.clash.ui.nav.TabbyNavDisplay
import com.github.kr328.clash.ui.theme.TabbyTheme

@Composable
fun TabbyApp(
  darkMode: DarkMode,
  backStack: List<NavKey>,
  entryProvider: (NavKey) -> NavEntry<NavKey>,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  TabbyTheme(darkMode = darkMode) {
    TabbyNavDisplay(
      backStack = backStack,
      modifier = modifier,
      onBack = onBack,
      entryProvider = entryProvider,
    )
  }
}
