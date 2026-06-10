package com.github.kr328.clash.common.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PatternsTest {
  @Test
  fun acceptsFileNamesThatMatchTheSharedPatternAndContainNonWhitespaceText() {
    assertTrue(tabbyIsValidFileNameInput("config.yaml"))
    assertTrue(tabbyIsValidFileNameInput("profile 1.yaml"))
    assertTrue(tabbyIsValidFileNameInput(".hidden"))
  }

  @Test
  fun rejectsEmptyBlankOrReservedCharacterFileNames() {
    assertFalse(tabbyIsValidFileNameInput(""))
    assertFalse(tabbyIsValidFileNameInput("   "))
    assertFalse(tabbyIsValidFileNameInput("profile/config.yaml"))
    assertFalse(tabbyIsValidFileNameInput("profile\nconfig.yaml"))
    assertFalse(tabbyIsValidFileNameInput("profile*config.yaml"))
    assertFalse(tabbyIsValidFileNameInput("profile&config.yaml"))
    assertFalse(tabbyIsValidFileNameInput("profile%config.yaml"))
  }
}
