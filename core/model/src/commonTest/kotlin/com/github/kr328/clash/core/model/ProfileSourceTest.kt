package com.github.kr328.clash.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
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

  @Test
  fun supportedProfileSourceSchemeAcceptsHttpHttpsAndContentCaseInsensitively() {
    assertTrue(isSupportedProfileSourceScheme("http"))
    assertTrue(isSupportedProfileSourceScheme("https"))
    assertTrue(isSupportedProfileSourceScheme("content"))
    assertTrue(isSupportedProfileSourceScheme("HTTP"))
    assertTrue(isSupportedProfileSourceScheme("HTTPS"))
    assertTrue(isSupportedProfileSourceScheme("CONTENT"))
  }

  @Test
  fun supportedProfileSourceSchemeRejectsMissingBlankAndOtherSchemes() {
    assertFalse(isSupportedProfileSourceScheme(null))
    assertFalse(isSupportedProfileSourceScheme(""))
    assertFalse(isSupportedProfileSourceScheme(" "))
    assertFalse(isSupportedProfileSourceScheme("file"))
  }

  @Test
  fun profileSubscriptionUserInfoFetchAcceptsOnlyUrlHttpsSources() {
    assertTrue(
      profileShouldFetchSubscriptionUserInfo(
        type = Profile.Type.Url,
        source = "https://example.com/config.yaml",
      )
    )
    assertTrue(
      profileShouldFetchSubscriptionUserInfo(
        type = Profile.Type.Url,
        source = "HTTPS://example.com/config.yaml",
      )
    )

    assertFalse(
      profileShouldFetchSubscriptionUserInfo(
        type = Profile.Type.Url,
        source = "http://example.com/config.yaml",
      )
    )
    assertFalse(
      profileShouldFetchSubscriptionUserInfo(
        type = Profile.Type.Url,
        source = "content://profiles/config.yaml",
      )
    )
    assertFalse(profileShouldFetchSubscriptionUserInfo(type = Profile.Type.Url, source = ""))
    assertFalse(
      profileShouldFetchSubscriptionUserInfo(
        type = Profile.Type.File,
        source = "https://example.com/config.yaml",
      )
    )
    assertFalse(
      profileShouldFetchSubscriptionUserInfo(
        type = Profile.Type.External,
        source = "https://example.com/config.yaml",
      )
    )
  }

  @Test
  fun profileConfigurationFetchAcceptsOnlyHttpSourcesThatAreForcedOrUncached() {
    assertTrue(
      profileShouldFetchConfiguration(
        source = "https://example.com/config.yaml",
        force = false,
        cachedConfigurationExists = false,
      )
    )
    assertTrue(
      profileShouldFetchConfiguration(
        source = "HTTP://example.com/config.yaml",
        force = true,
        cachedConfigurationExists = true,
      )
    )

    assertFalse(
      profileShouldFetchConfiguration(
        source = "https://example.com/config.yaml",
        force = false,
        cachedConfigurationExists = true,
      )
    )
    assertFalse(
      profileShouldFetchConfiguration(
        source = "content://profiles/config.yaml",
        force = true,
        cachedConfigurationExists = false,
      )
    )
    assertFalse(
      profileShouldFetchConfiguration(
        source = "",
        force = true,
        cachedConfigurationExists = false,
      )
    )
  }

  @Test
  fun profileConfigurationFetchStatusUsesSourceHostOrEmptyFallback() {
    assertEquals(
      FetchStatus(
        action = FetchStatus.Action.FetchConfiguration,
        args = listOf("example.com"),
        progress = -1,
        max = -1,
      ),
      profileFetchConfigurationStatus("example.com"),
    )

    assertEquals(
      FetchStatus(
        action = FetchStatus.Action.FetchConfiguration,
        args = listOf(""),
        progress = -1,
        max = -1,
      ),
      profileFetchConfigurationStatus(null),
    )
  }

  @Test
  fun profileConfigurationFetchIsForcedOnlyForNonFileProfiles() {
    assertFalse(profileShouldForceFetchConfiguration(Profile.Type.File))
    assertTrue(profileShouldForceFetchConfiguration(Profile.Type.Url))
    assertTrue(profileShouldForceFetchConfiguration(Profile.Type.External))
  }

  @Test
  fun profileValidationConfigurationFetchIsForcedOnlyWhenRequiredAndNotAlreadyFetched() {
    assertTrue(
      profileShouldForceFetchValidationConfiguration(
        forceConfigurationFetch = true,
        configurationFetched = false,
      )
    )

    assertFalse(
      profileShouldForceFetchValidationConfiguration(
        forceConfigurationFetch = true,
        configurationFetched = true,
      )
    )
    assertFalse(
      profileShouldForceFetchValidationConfiguration(
        forceConfigurationFetch = false,
        configurationFetched = false,
      )
    )
  }
}
