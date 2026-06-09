package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class PropertiesSourceInputValidationTest {
  @Test
  fun profilePropertiesSourceInputAcceptsHttpSourcesCaseInsensitively() {
    assertEquals(
      "https://example.com/config.yaml",
      profilePropertiesSourceInputValue("https://example.com/config.yaml"),
    )
    assertEquals(
      "http://example.com/config.yaml",
      profilePropertiesSourceInputValue("http://example.com/config.yaml"),
    )
    assertEquals(
      "HTTPS://example.com/config.yaml",
      profilePropertiesSourceInputValue("HTTPS://example.com/config.yaml"),
    )
    assertEquals(
      "HTTP://example.com/config.yaml",
      profilePropertiesSourceInputValue("HTTP://example.com/config.yaml"),
    )
  }

  @Test
  fun profilePropertiesSourceInputRejectsNonHttpSources() {
    listOf(
        "content://profiles/config.yaml",
        "file:///profiles/config.yaml",
        "/profiles/config.yaml",
        "",
      )
      .forEach { source ->
        assertEquals(null, profilePropertiesSourceInputValue(source))
      }
  }

  @Test
  fun profilePropertiesSourceInputDoesNotTrimBeforeCheckingScheme() {
    assertEquals(null, profilePropertiesSourceInputValue(" https://example.com/config.yaml"))
    assertEquals(null, profilePropertiesSourceInputValue("\thttp://example.com/config.yaml"))
  }
}
