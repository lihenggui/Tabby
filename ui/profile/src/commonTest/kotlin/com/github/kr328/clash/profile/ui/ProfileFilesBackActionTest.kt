package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileFilesBackActionTest {
  @Test
  fun finishesAtBaseDirectory() {
    val location = ProfileFilesLocation().initialize("root")

    assertEquals(ProfileFilesBackAction.Finish, profileFilesBackAction(location))
  }

  @Test
  fun leavesNestedDirectories() {
    val location =
      ProfileFilesLocation().initialize("root").enterDirectory("providers").enterDirectory("rules")

    assertEquals(
      ProfileFilesBackAction.LeaveDirectory(
        ProfileFilesLocation(rootDocumentId = "root", directoryStack = listOf("providers"))
      ),
      profileFilesBackAction(location),
    )
  }
}
