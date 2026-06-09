package com.github.kr328.clash.app

data class TabbyExternalQuickActionShortcut(
  val id: String,
  val action: TabbyExternalQuickAction,
  val rank: Int,
  val presentation: TabbyExternalQuickActionShortcutPresentation,
)

data class TabbyExternalQuickActionShortcutLaunchOptions(
  val openInNewTask: Boolean,
  val excludeFromRecents: Boolean,
  val noAnimation: Boolean,
)

data class TabbyExternalQuickActionShortcutResources(
  val shortLabel: Int,
  val longLabel: Int,
  val icon: Int,
)

enum class TabbyExternalQuickActionShortcutPresentation {
  ToggleClash,
  StartClash,
  StopClash,
}

sealed interface TabbyExternalQuickActionShortcutPlan {
  data class Install(
    val shortcuts: List<TabbyExternalQuickActionShortcut>,
    val launchOptions: TabbyExternalQuickActionShortcutLaunchOptions,
  ) : TabbyExternalQuickActionShortcutPlan

  data object Skip : TabbyExternalQuickActionShortcutPlan
}

fun tabbyExternalQuickActionShortcuts(): List<TabbyExternalQuickActionShortcut> =
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

fun tabbyExternalQuickActionShortcutLaunchOptions(): TabbyExternalQuickActionShortcutLaunchOptions =
  TabbyExternalQuickActionShortcutLaunchOptions(
    openInNewTask = true,
    excludeFromRecents = true,
    noAnimation = true,
  )

fun tabbyExternalQuickActionShortcutPresentationResources(
  presentation: TabbyExternalQuickActionShortcutPresentation,
  toggleShortLabel: Int,
  toggleLongLabel: Int,
  toggleIcon: Int,
  startShortLabel: Int,
  startLongLabel: Int,
  startIcon: Int,
  stopShortLabel: Int,
  stopLongLabel: Int,
  stopIcon: Int,
): TabbyExternalQuickActionShortcutResources =
  when (presentation) {
    TabbyExternalQuickActionShortcutPresentation.ToggleClash ->
      TabbyExternalQuickActionShortcutResources(
        shortLabel = toggleShortLabel,
        longLabel = toggleLongLabel,
        icon = toggleIcon,
      )
    TabbyExternalQuickActionShortcutPresentation.StartClash ->
      TabbyExternalQuickActionShortcutResources(
        shortLabel = startShortLabel,
        longLabel = startLongLabel,
        icon = startIcon,
      )
    TabbyExternalQuickActionShortcutPresentation.StopClash ->
      TabbyExternalQuickActionShortcutResources(
        shortLabel = stopShortLabel,
        longLabel = stopLongLabel,
        icon = stopIcon,
      )
  }

fun tabbyExternalQuickActionShortcutLaunchFlags(
  launchOptions: TabbyExternalQuickActionShortcutLaunchOptions,
  openInNewTaskFlag: Int,
  excludeFromRecentsFlag: Int,
  noAnimationFlag: Int,
): Int {
  var flags = 0
  if (launchOptions.openInNewTask) flags = flags or openInNewTaskFlag
  if (launchOptions.excludeFromRecents) flags = flags or excludeFromRecentsFlag
  if (launchOptions.noAnimation) flags = flags or noAnimationFlag
  return flags
}

fun tabbyExternalQuickActionShortcutPlan(
  appIconHidden: Boolean
): TabbyExternalQuickActionShortcutPlan =
  if (appIconHidden) {
    TabbyExternalQuickActionShortcutPlan.Skip
  } else {
    TabbyExternalQuickActionShortcutPlan.Install(
      shortcuts = tabbyExternalQuickActionShortcuts(),
      launchOptions = tabbyExternalQuickActionShortcutLaunchOptions(),
    )
  }
