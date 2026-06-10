package com.github.kr328.clash.app

import com.github.kr328.clash.core.model.DarkMode

enum class TabbyEdgeToEdgeStyle {
  Auto,
  ForceLight,
  ForceDark,
}

data class TabbyEdgeToEdgeSystemBarMode(val forcedDarkMode: Boolean?)

fun tabbyEdgeToEdgeStyle(darkMode: DarkMode): TabbyEdgeToEdgeStyle =
  when (darkMode) {
    DarkMode.Auto -> TabbyEdgeToEdgeStyle.Auto
    DarkMode.ForceLight -> TabbyEdgeToEdgeStyle.ForceLight
    DarkMode.ForceDark -> TabbyEdgeToEdgeStyle.ForceDark
  }

fun tabbyEdgeToEdgeSystemBarMode(style: TabbyEdgeToEdgeStyle): TabbyEdgeToEdgeSystemBarMode =
  TabbyEdgeToEdgeSystemBarMode(
    forcedDarkMode =
      when (style) {
        TabbyEdgeToEdgeStyle.Auto -> null
        TabbyEdgeToEdgeStyle.ForceLight -> false
        TabbyEdgeToEdgeStyle.ForceDark -> true
      }
  )
