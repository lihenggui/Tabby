package com.github.kr328.clash.app

data class TabbyExternalQuickActionShortcut(
  val id: String,
  val action: TabbyExternalQuickAction,
  val rank: Int,
)

sealed interface TabbyExternalQuickActionShortcutPlan {
  data class Install(val shortcuts: List<TabbyExternalQuickActionShortcut>) :
    TabbyExternalQuickActionShortcutPlan

  data object Skip : TabbyExternalQuickActionShortcutPlan
}

fun tabbyExternalQuickActionShortcuts(): List<TabbyExternalQuickActionShortcut> =
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

fun tabbyExternalQuickActionShortcutPlan(
  appIconHidden: Boolean
): TabbyExternalQuickActionShortcutPlan =
  if (appIconHidden) {
    TabbyExternalQuickActionShortcutPlan.Skip
  } else {
    TabbyExternalQuickActionShortcutPlan.Install(tabbyExternalQuickActionShortcuts())
  }
