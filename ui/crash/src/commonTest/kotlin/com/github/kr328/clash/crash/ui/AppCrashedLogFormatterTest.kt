package com.github.kr328.clash.crash.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class AppCrashedLogFormatterTest {
  @Test
  fun separatorLinesAreRemoved() {
    val log =
      formatAppCrashLog(
        listOf(
          "------ beginning of crash",
          "AndroidRuntime: crash",
          "------ end",
          "Tabby: marker",
        )
      )

    assertEquals("AndroidRuntime: crash\nTabby: marker", log)
  }

  @Test
  fun formattedLogIsTrimmed() {
    val log = formatAppCrashLog(listOf("", "  AndroidRuntime: crash  ", ""))

    assertEquals("AndroidRuntime: crash", log)
  }

  @Test
  fun separatorWithLeadingWhitespaceIsNotFiltered() {
    val log = formatAppCrashLog(listOf(" ------ not a separator", "Tabby: marker"))

    assertEquals("------ not a separator\nTabby: marker", log)
  }
}
