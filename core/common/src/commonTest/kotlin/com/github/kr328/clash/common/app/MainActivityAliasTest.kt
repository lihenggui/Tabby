package com.github.kr328.clash.common.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MainActivityAliasTest {
  @Test
  fun returnsFirstLauncherAliasTargetingMainActivity() {
    assertEquals(
      TabbyActivityComponentSpec(packageName = "io.github.goooler.tabby", name = "AliasActivity"),
      tabbyMainActivityAliasFromLauncherActivities(
        mainActivityName = MAIN_ACTIVITY_NAME,
        launcherActivities =
          sequenceOf(
            TabbyLauncherActivitySpec(
              packageName = "io.github.goooler.tabby",
              name = "OtherAliasActivity",
              targetActivity = "com.github.kr328.clash.app.OtherActivity",
            ),
            TabbyLauncherActivitySpec(
              packageName = "io.github.goooler.tabby",
              name = "AliasActivity",
              targetActivity = MAIN_ACTIVITY_NAME,
            ),
            TabbyLauncherActivitySpec(
              packageName = "io.github.goooler.tabby",
              name = "SecondAliasActivity",
              targetActivity = MAIN_ACTIVITY_NAME,
            ),
          ),
      ),
    )
  }

  @Test
  fun ignoresLauncherActivitiesWithoutTargetActivity() {
    assertEquals(
      TabbyActivityComponentSpec(packageName = "io.github.goooler.tabby", name = "AliasActivity"),
      tabbyMainActivityAliasFromLauncherActivities(
        mainActivityName = MAIN_ACTIVITY_NAME,
        launcherActivities =
          sequenceOf(
            TabbyLauncherActivitySpec(
              packageName = "io.github.goooler.tabby",
              name = "DirectActivity",
              targetActivity = null,
            ),
            TabbyLauncherActivitySpec(
              packageName = "io.github.goooler.tabby",
              name = "AliasActivity",
              targetActivity = MAIN_ACTIVITY_NAME,
            ),
          ),
      ),
    )
  }

  @Test
  fun returnsNullWhenNoLauncherAliasTargetsMainActivity() {
    assertNull(
      tabbyMainActivityAliasFromLauncherActivities(
        mainActivityName = MAIN_ACTIVITY_NAME,
        launcherActivities =
          sequenceOf(
            TabbyLauncherActivitySpec(
              packageName = "io.github.goooler.tabby",
              name = "OtherAliasActivity",
              targetActivity = "com.github.kr328.clash.app.OtherActivity",
            )
          ),
      )
    )
  }

  @Test
  fun treatsEnabledAndDefaultComponentStatesAsVisibleByDefault() {
    assertFalse(
      tabbyMainActivityAliasHiddenByDefault(
        componentState = ENABLED_COMPONENT_STATE,
        enabledState = ENABLED_COMPONENT_STATE,
        defaultState = DEFAULT_COMPONENT_STATE,
      )
    )
    assertFalse(
      tabbyMainActivityAliasHiddenByDefault(
        componentState = DEFAULT_COMPONENT_STATE,
        enabledState = ENABLED_COMPONENT_STATE,
        defaultState = DEFAULT_COMPONENT_STATE,
      )
    )
  }

  @Test
  fun treatsOtherComponentStatesAsHiddenByDefault() {
    assertTrue(
      tabbyMainActivityAliasHiddenByDefault(
        componentState = DISABLED_COMPONENT_STATE,
        enabledState = ENABLED_COMPONENT_STATE,
        defaultState = DEFAULT_COMPONENT_STATE,
      )
    )
  }
}

private const val MAIN_ACTIVITY_NAME = "com.github.kr328.clash.app.MainActivity"
private const val DEFAULT_COMPONENT_STATE = 0
private const val ENABLED_COMPONENT_STATE = 1
private const val DISABLED_COMPONENT_STATE = 2
