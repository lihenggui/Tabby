package com.github.kr328.clash.network

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileUserAgentTest {
  @Test
  fun usesVersionNameInUserAgent() {
    assertEquals("Tabby/1.2.3", tabbyProfileUserAgent("1.2.3"))
  }

  @Test
  fun usesUnknownWhenVersionNameIsMissing() {
    assertEquals("Tabby/unknown", tabbyProfileUserAgent(null))
  }

  @Test
  fun preservesBlankVersionName() {
    assertEquals("Tabby/", tabbyProfileUserAgent(""))
  }
}
