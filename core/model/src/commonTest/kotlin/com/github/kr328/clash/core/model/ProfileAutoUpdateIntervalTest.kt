package com.github.kr328.clash.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProfileAutoUpdateIntervalTest {
  @Test
  fun profileAutoUpdateIntervalMinutesInputAcceptsEmptyAndMinimumMinutes() {
    assertTrue(isProfileAutoUpdateIntervalMinutesInput(""))
    assertTrue(isProfileAutoUpdateIntervalMinutesInput("15"))
    assertTrue(isProfileAutoUpdateIntervalMinutesInput("120"))
  }

  @Test
  fun profileAutoUpdateIntervalMinutesInputRejectsInvalidOrSmallMinutes() {
    assertFalse(isProfileAutoUpdateIntervalMinutesInput("14"))
    assertFalse(isProfileAutoUpdateIntervalMinutesInput("-1"))
    assertFalse(isProfileAutoUpdateIntervalMinutesInput("abc"))
    assertFalse(isProfileAutoUpdateIntervalMinutesInput(" 15"))
  }

  @Test
  fun profileAutoUpdateIntervalMillisFromMinutesInputMapsValidInput() {
    assertEquals(0L, profileAutoUpdateIntervalMillisFromMinutesInput(""))
    assertEquals(900_000L, profileAutoUpdateIntervalMillisFromMinutesInput("15"))
    assertEquals(7_200_000L, profileAutoUpdateIntervalMillisFromMinutesInput("120"))
  }

  @Test
  fun profileAutoUpdateIntervalMillisFromMinutesInputRejectsInvalidInput() {
    assertEquals(null, profileAutoUpdateIntervalMillisFromMinutesInput("14"))
    assertEquals(null, profileAutoUpdateIntervalMillisFromMinutesInput("-1"))
    assertEquals(null, profileAutoUpdateIntervalMillisFromMinutesInput("abc"))
    assertEquals(null, profileAutoUpdateIntervalMillisFromMinutesInput(" 15"))
  }

  @Test
  fun profileAutoUpdateIntervalMillisAcceptsDisabledAndMinimumMillis() {
    assertTrue(isValidProfileAutoUpdateIntervalMillis(0))
    assertTrue(isValidProfileAutoUpdateIntervalMillis(900_000))
    assertTrue(isValidProfileAutoUpdateIntervalMillis(7_200_000))
  }

  @Test
  fun profileAutoUpdateIntervalMillisRejectsNegativeAndSmallMillis() {
    assertFalse(isValidProfileAutoUpdateIntervalMillis(-1))
    assertFalse(isValidProfileAutoUpdateIntervalMillis(1))
    assertFalse(isValidProfileAutoUpdateIntervalMillis(899_999))
  }
}
