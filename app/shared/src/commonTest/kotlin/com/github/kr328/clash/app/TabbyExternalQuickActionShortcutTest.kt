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
      TabbyExternalQuickActionShortcutPlan.Install(
        shortcuts = expectedQuickActionShortcuts,
        launchOptions = expectedQuickActionShortcutLaunchOptions,
      ),
      tabbyExternalQuickActionShortcutPlan(appIconHidden = false),
    )
  }

  @Test
  fun tabbyExternalQuickActionShortcutLaunchOptionsUseTaskSafeDefaults() {
    assertEquals(
      expectedQuickActionShortcutLaunchOptions,
      tabbyExternalQuickActionShortcutLaunchOptions(),
    )
  }
}

private val expectedQuickActionShortcutLaunchOptions =
  TabbyExternalQuickActionShortcutLaunchOptions(
    openInNewTask = true,
    excludeFromRecents = true,
    noAnimation = true,
  )

private val expectedQuickActionShortcuts =
  listOf(
    TabbyExternalQuickActionShortcut(
      id = "toggle_clash",
      action = TabbyExternalQuickAction.ToggleClash,
      rank = 0,
      presentation = TabbyExternalQuickActionShortcutPresentation.ToggleClash,
    ),
    TabbyExternalQuickActionShortcut(
      id = "start_clash",
      action = TabbyExternalQuickAction.StartClash,
      rank = 1,
      presentation = TabbyExternalQuickActionShortcutPresentation.StartClash,
    ),
    TabbyExternalQuickActionShortcut(
      id = "stop_clash",
      action = TabbyExternalQuickAction.StopClash,
      rank = 2,
      presentation = TabbyExternalQuickActionShortcutPresentation.StopClash,
    ),
  )
