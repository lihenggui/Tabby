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
  fun tabbyExternalQuickActionShortcutPresentationResourcesMapsPresentationTokens() {
    assertEquals(
      TabbyExternalQuickActionShortcutResources(
        shortLabel = 1,
        longLabel = 2,
        icon = 3,
      ),
      tabbyExternalQuickActionShortcutPresentationResources(
        presentation = TabbyExternalQuickActionShortcutPresentation.ToggleClash,
        toggleShortLabel = 1,
        toggleLongLabel = 2,
        toggleIcon = 3,
        startShortLabel = 4,
        startLongLabel = 5,
        startIcon = 6,
        stopShortLabel = 7,
        stopLongLabel = 8,
        stopIcon = 9,
      ),
    )
    assertEquals(
      TabbyExternalQuickActionShortcutResources(
        shortLabel = 4,
        longLabel = 5,
        icon = 6,
      ),
      tabbyExternalQuickActionShortcutPresentationResources(
        presentation = TabbyExternalQuickActionShortcutPresentation.StartClash,
        toggleShortLabel = 1,
        toggleLongLabel = 2,
        toggleIcon = 3,
        startShortLabel = 4,
        startLongLabel = 5,
        startIcon = 6,
        stopShortLabel = 7,
        stopLongLabel = 8,
        stopIcon = 9,
      ),
    )
    assertEquals(
      TabbyExternalQuickActionShortcutResources(
        shortLabel = 7,
        longLabel = 8,
        icon = 9,
      ),
      tabbyExternalQuickActionShortcutPresentationResources(
        presentation = TabbyExternalQuickActionShortcutPresentation.StopClash,
        toggleShortLabel = 1,
        toggleLongLabel = 2,
        toggleIcon = 3,
        startShortLabel = 4,
        startLongLabel = 5,
        startIcon = 6,
        stopShortLabel = 7,
        stopLongLabel = 8,
        stopIcon = 9,
      ),
    )
  }

  @Test
  fun tabbyExternalQuickActionShortcutLaunchFlagsCombinesEnabledFlagValues() {
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
