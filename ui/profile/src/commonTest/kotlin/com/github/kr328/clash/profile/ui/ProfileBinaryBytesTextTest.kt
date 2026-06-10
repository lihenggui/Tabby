package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileBinaryBytesTextTest {
  @Test
  fun formatsByteValues() {
    assertEquals("0 B", profileBinaryBytesText(0))
    assertEquals("512 B", profileBinaryBytesText(512))
    assertEquals("0 B", profileBinaryBytesText(-1))
  }

  @Test
  fun formatsBinaryUnitsWithOneDecimal() {
    assertEquals("1 KiB", profileBinaryBytesText(1024))
    assertEquals("1.5 KiB", profileBinaryBytesText(1536))
    assertEquals("5.5 MiB", profileBinaryBytesText(5 * 1024 * 1024 + 512 * 1024))
  }

  @Test
  fun roundsBinaryUnits() {
    assertEquals("1.1 KiB", profileBinaryBytesText(1126))
    assertEquals("2 MiB", profileBinaryBytesText(2 * 1024 * 1024 + 1))
  }
}
