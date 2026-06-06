package com.github.kr328.clash.app

import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyExternalQuickActionShortcutTest {
  @Test
  fun tabbyExternalQuickActionShortcutsKeepStableIdsActionsAndRanks() {
    assertEquals(
      expectedQuickActionShortcuts,
      tabbyExternalQuickActionShortcuts(),
    )
  }

  @Test
  fun tabbyExternalQuickActionShortcutPlanSkipsWhenAppIconIsHidden() {
    assertEquals(
      TabbyExternalQuickActionShortcutPlan.Skip,
      tabbyExternalQuickActionShortcutPlan(appIconHidden = true),
    )
  }

  @Test
  fun tabbyExternalQuickActionShortcutPlanInstallsShortcutsWhenAppIconIsVisible() {
    assertEquals(
      TabbyExternalQuickActionShortcutPlan.Install(expectedQuickActionShortcuts),
      tabbyExternalQuickActionShortcutPlan(appIconHidden = false),
    )
  }
}

private val expectedQuickActionShortcuts =
  listOf(
    TabbyExternalQuickActionShortcut(
      id = "toggle_clash",
      action = TabbyExternalQuickAction.ToggleClash,
      rank = 0,
    ),
    TabbyExternalQuickActionShortcut(
      id = "start_clash",
      action = TabbyExternalQuickAction.StartClash,
      rank = 1,
    ),
    TabbyExternalQuickActionShortcut(
      id = "stop_clash",
      action = TabbyExternalQuickAction.StopClash,
      rank = 2,
    ),
  )
