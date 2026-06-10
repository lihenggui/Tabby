package com.github.kr328.clash.common.app

data class TabbyLauncherActivitySpec(
  val packageName: String,
  val name: String,
  val targetActivity: String?,
)

data class TabbyActivityComponentSpec(
  val packageName: String,
  val name: String,
)

fun tabbyMainActivityAliasFromLauncherActivities(
  mainActivityName: String,
  launcherActivities: Sequence<TabbyLauncherActivitySpec>,
): TabbyActivityComponentSpec? {
  return launcherActivities
    .firstOrNull { it.targetActivity == mainActivityName }
    ?.let { TabbyActivityComponentSpec(packageName = it.packageName, name = it.name) }
}

fun tabbyMainActivityAliasHiddenByDefault(
  componentState: Int,
  enabledState: Int,
  defaultState: Int,
): Boolean {
  return componentState != enabledState && componentState != defaultState
}
