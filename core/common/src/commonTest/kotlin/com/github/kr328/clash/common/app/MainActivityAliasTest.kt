package com.github.kr328.clash.common.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
}

private const val MAIN_ACTIVITY_NAME = "com.github.kr328.clash.app.MainActivity"
