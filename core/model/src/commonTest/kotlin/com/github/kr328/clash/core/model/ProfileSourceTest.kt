package com.github.kr328.clash.core.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProfileSourceTest {
  @Test
  fun httpProfileSourceAcceptsHttpAndHttpsSourcesCaseInsensitively() {
    assertTrue(isHttpProfileSource("https://example.com/config.yaml"))
    assertTrue(isHttpProfileSource("http://example.com/config.yaml"))
    assertTrue(isHttpProfileSource("HTTPS://example.com/config.yaml"))
    assertTrue(isHttpProfileSource("HTTP://example.com/config.yaml"))
  }

  @Test
  fun httpProfileSourceRejectsNonHttpSources() {
    assertFalse(isHttpProfileSource("content://profiles/config.yaml"))
    assertFalse(isHttpProfileSource("file:///profiles/config.yaml"))
    assertFalse(isHttpProfileSource("/profiles/config.yaml"))
    assertFalse(isHttpProfileSource(""))
  }

  @Test
  fun httpProfileSourceDoesNotTrimBeforeCheckingScheme() {
    assertFalse(isHttpProfileSource(" https://example.com/config.yaml"))
    assertFalse(isHttpProfileSource("\thttp://example.com/config.yaml"))
  }

  @Test
  fun httpsProfileSourceAcceptsHttpsSourcesCaseInsensitively() {
    assertTrue(isHttpsProfileSource("https://example.com/config.yaml"))
    assertTrue(isHttpsProfileSource("HTTPS://example.com/config.yaml"))
  }

  @Test
  fun httpsProfileSourceRejectsHttpAndNonHttpSources() {
    assertFalse(isHttpsProfileSource("http://example.com/config.yaml"))
    assertFalse(isHttpsProfileSource("content://profiles/config.yaml"))
    assertFalse(isHttpsProfileSource("file:///profiles/config.yaml"))
    assertFalse(isHttpsProfileSource(""))
  }

  @Test
  fun httpsProfileSourceDoesNotTrimBeforeCheckingScheme() {
    assertFalse(isHttpsProfileSource(" https://example.com/config.yaml"))
    assertFalse(isHttpsProfileSource("\thttps://example.com/config.yaml"))
  }
}
