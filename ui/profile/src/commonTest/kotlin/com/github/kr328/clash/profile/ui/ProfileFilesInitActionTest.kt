package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class ProfileFilesInitActionTest {
  @Test
  fun initializesUninitializedLocation() {
    assertEquals(
      ProfileFilesInitAction.Initialize(ProfileFilesLocation(rootDocumentId = "profile-root")),
      profileFilesInitAction(ProfileFilesLocation(), rootDocumentId = "profile-root"),
    )
  }

  @Test
  fun ignoresAlreadyInitializedLocation() {
    val location = ProfileFilesLocation().initialize("profile-root")

    assertEquals(
      ProfileFilesInitAction.Ignore,
      profileFilesInitAction(location, rootDocumentId = "ignored-root"),
    )
  }

  @Test
  fun finishesWhenProfileIsMissing() {
    assertEquals(ProfileFilesLoadedAction.Finish, profileFilesLoadedAction(null))
  }

  @Test
  fun loadsFilesWithEditableConfigurationForUrlProfiles() {
    assertEquals(
      ProfileFilesLoadedAction.LoadFiles(configurationEditable = true),
      profileFilesLoadedAction(profile(Profile.Type.Url)),
    )
  }

  @Test
  fun loadsFilesWithReadOnlyConfigurationForFileProfiles() {
    assertEquals(
      ProfileFilesLoadedAction.LoadFiles(configurationEditable = false),
      profileFilesLoadedAction(profile(Profile.Type.File)),
    )
  }

  private fun profile(type: Profile.Type): Profile {
    return Profile(
      uuid = Uuid.parse("00000000-0000-0000-0000-000000000001"),
      name = "Profile",
      type = type,
      source = "",
      active = false,
      interval = 0,
      upload = 0,
      download = 0,
      total = 0,
      expire = 0,
      updatedAt = 0,
      imported = true,
      pending = false,
    )
  }
}
