package com.github.kr328.clash.crash

import kotlin.test.Test
import kotlin.test.assertEquals

class CrashLinksTest {
  @Test
  fun tabbyGithubUsesTabbyRepository() {
    assertEquals("Goooler/Tabby", TABBY_REPO)
    assertEquals("https://github.com/Goooler/Tabby", TABBY_GITHUB)
  }
}
