package com.github.kr328.clash.app

import com.github.kr328.clash.core.model.DarkMode
import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyEdgeToEdgeStyleTest {
  @Test
  fun tabbyEdgeToEdgeStyleMapsAutoDarkMode() {
    assertEquals(
      TabbyEdgeToEdgeStyle.Auto,
      tabbyEdgeToEdgeStyle(DarkMode.Auto),
    )
  }

  @Test
  fun tabbyEdgeToEdgeStyleMapsForceLightDarkMode() {
    assertEquals(
      TabbyEdgeToEdgeStyle.ForceLight,
      tabbyEdgeToEdgeStyle(DarkMode.ForceLight),
    )
  }

  @Test
  fun tabbyEdgeToEdgeStyleMapsForceDarkDarkMode() {
    assertEquals(
      TabbyEdgeToEdgeStyle.ForceDark,
      tabbyEdgeToEdgeStyle(DarkMode.ForceDark),
    )
  }

  @Test
  fun tabbyEdgeToEdgeSystemBarModeKeepsAutoDetectionForAutoStyle() {
    assertEquals(
      TabbyEdgeToEdgeSystemBarMode(forcedDarkMode = null),
      tabbyEdgeToEdgeSystemBarMode(TabbyEdgeToEdgeStyle.Auto),
    )
  }

  @Test
  fun tabbyEdgeToEdgeSystemBarModeForcesLightOrDarkDetection() {
    assertEquals(
      TabbyEdgeToEdgeSystemBarMode(forcedDarkMode = false),
      tabbyEdgeToEdgeSystemBarMode(TabbyEdgeToEdgeStyle.ForceLight),
    )
    assertEquals(
      TabbyEdgeToEdgeSystemBarMode(forcedDarkMode = true),
      tabbyEdgeToEdgeSystemBarMode(TabbyEdgeToEdgeStyle.ForceDark),
    )
  }
}
