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

  @Test
  fun tabbyExternalQuickActionShortcutLaunchFlagsCombinesEnabledPlatformFlags() {
    assertEquals(
      0b111,
      tabbyExternalQuickActionShortcutLaunchFlags(
        launchOptions = expectedQuickActionShortcutLaunchOptions,
        openInNewTaskFlag = 0b001,
        excludeFromRecentsFlag = 0b010,
        noAnimationFlag = 0b100,
      ),
    )
    assertEquals(
      0b101,
      tabbyExternalQuickActionShortcutLaunchFlags(
        launchOptions =
          TabbyExternalQuickActionShortcutLaunchOptions(
            openInNewTask = true,
            excludeFromRecents = false,
            noAnimation = true,
          ),
        openInNewTaskFlag = 0b001,
        excludeFromRecentsFlag = 0b010,
        noAnimationFlag = 0b100,
      ),
    )
  }

  @Test
  fun tabbyExternalQuickActionShortcutLaunchFlagsReturnsZeroWhenAllOptionsAreDisabled() {
    assertEquals(
      0,
      tabbyExternalQuickActionShortcutLaunchFlags(
        launchOptions =
          TabbyExternalQuickActionShortcutLaunchOptions(
            openInNewTask = false,
            excludeFromRecents = false,
            noAnimation = false,
          ),
        openInNewTaskFlag = 0b001,
        excludeFromRecentsFlag = 0b010,
        noAnimationFlag = 0b100,
      ),
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
