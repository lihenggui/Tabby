package com.github.kr328.clash.app

import com.github.kr328.clash.core.model.DarkMode

enum class TabbyEdgeToEdgeStyle {
  Auto,
  ForceLight,
  ForceDark,
}

fun tabbyEdgeToEdgeStyle(darkMode: DarkMode): TabbyEdgeToEdgeStyle =
  when (darkMode) {
    DarkMode.Auto -> TabbyEdgeToEdgeStyle.Auto
    DarkMode.ForceLight -> TabbyEdgeToEdgeStyle.ForceLight
    DarkMode.ForceDark -> TabbyEdgeToEdgeStyle.ForceDark
  }
