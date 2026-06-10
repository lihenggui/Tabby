package com.github.kr328.clash.common.id

import kotlin.test.Test
import kotlin.test.assertEquals

class UndefinedIdTest {
  @Test
  fun createsFirstUndefinedIdFromZero() {
    assertEquals(0x14000001, tabbyNextUndefinedId(current = 0))
  }

  @Test
  fun incrementsOnlyTheMaskedCounterBits() {
    assertEquals(0x14345679, tabbyNextUndefinedId(current = 0x12345678))
  }

  @Test
  fun preservesBoundaryCarryBehavior() {
    assertEquals(0x15000000, tabbyNextUndefinedId(current = 0x14FFFFFF))
    assertEquals(0x14000001, tabbyNextUndefinedId(current = 0x15000000))
  }
}
