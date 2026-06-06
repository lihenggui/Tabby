package com.github.kr328.clash.log

import kotlin.test.Test
import kotlin.test.assertEquals

class LogcatStatusLaunchFlagsTest {
  @Test
  fun combinesRequiredStatusLaunchFlags() {
    assertEquals(
      0b111,
      logcatStatusLaunchFlags(
        newTaskFlag = 0b001,
        singleTopFlag = 0b010,
        clearTopFlag = 0b100,
      ),
    )
  }

  @Test
  fun preservesOverlappingStatusLaunchFlags() {
    assertEquals(
      0b011,
      logcatStatusLaunchFlags(
        newTaskFlag = 0b001,
        singleTopFlag = 0b010,
        clearTopFlag = 0b001,
      ),
    )
  }
}
