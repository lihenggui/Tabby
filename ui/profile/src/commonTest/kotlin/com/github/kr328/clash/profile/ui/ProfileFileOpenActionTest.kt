package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileFileOpenActionTest {
  @Test
  fun opensDirectoriesByEnteringThem() {
    assertEquals(
      ProfileFileOpenAction.EnterDirectory("root/providers"),
      profileFileOpenAction(documentId = "root/providers", isDirectory = true),
    )
  }

  @Test
  fun opensFilesAsDocuments() {
    assertEquals(
      ProfileFileOpenAction.OpenFile("root/config.yaml"),
      profileFileOpenAction(documentId = "root/config.yaml", isDirectory = false),
    )
  }
}
