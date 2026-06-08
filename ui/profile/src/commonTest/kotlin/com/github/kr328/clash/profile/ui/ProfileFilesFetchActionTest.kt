package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileFilesFetchActionTest {
  @Test
  fun ignoresUninitializedLocation() {
    assertEquals(ProfileFilesFetchAction.Ignore, profileFilesFetchAction(ProfileFilesLocation()))
  }

  @Test
  fun fetchesBaseDirectory() {
    val location = ProfileFilesLocation().initialize("profile-root")

    assertEquals(
      ProfileFilesFetchAction.Fetch(documentId = "profile-root", inBaseDirectory = true),
      profileFilesFetchAction(location),
    )
  }

  @Test
  fun fetchesNestedDirectory() {
    val location = ProfileFilesLocation().initialize("profile-root").enterDirectory("providers")

    assertEquals(
      ProfileFilesFetchAction.Fetch(documentId = "providers", inBaseDirectory = false),
      profileFilesFetchAction(location),
    )
  }

  @Test
  fun refreshRequestedFromPlatformLifecycleEventOnlyRequestsRefreshOnStart() {
    assertEquals(
      true,
      profileFilesRefreshRequestedFromPlatformLifecycleEvent(startEvent = true),
    )
    assertEquals(
      false,
      profileFilesRefreshRequestedFromPlatformLifecycleEvent(startEvent = false),
    )
  }
}
