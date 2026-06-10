package com.github.kr328.clash.common.document

import kotlin.test.Test
import kotlin.test.assertEquals

class DocumentFlagTest {
  @Test
  fun emptyDocumentFlagsProduceNoPlatformFlags() {
    assertEquals(
      0,
      tabbyDocumentPlatformFlags(
        flags = emptySet(),
        writableFlag = 0b001,
        deletableFlag = 0b010,
        virtualFlag = 0b100,
      ),
    )
  }

  @Test
  fun combinesDocumentFlagsWithPlatformFlagTokens() {
    assertEquals(
      0b111,
      tabbyDocumentPlatformFlags(
        flags = setOf(Flag.Writable, Flag.Deletable, Flag.Virtual),
        writableFlag = 0b001,
        deletableFlag = 0b010,
        virtualFlag = 0b100,
      ),
    )
  }
}
