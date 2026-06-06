package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProfileFilesLocationTest {
  @Test
  fun startsUninitializedAtBaseDirectory() {
    val location = ProfileFilesLocation()

    assertFalse(location.initialized)
    assertEquals("", location.currentDocumentId)
    assertTrue(location.currentInBaseDir)
  }

  @Test
  fun initializesRootDocumentIdOnce() {
    val location = ProfileFilesLocation().initialize("root-profile").initialize("ignored-profile")

    assertTrue(location.initialized)
    assertEquals("root-profile", location.rootDocumentId)
    assertEquals("root-profile", location.currentDocumentId)
    assertTrue(location.currentInBaseDir)
  }

  @Test
  fun entersNestedDirectories() {
    val location =
      ProfileFilesLocation()
        .initialize("root-profile")
        .enterDirectory("providers")
        .enterDirectory("rules")

    assertEquals(listOf("providers", "rules"), location.directoryStack)
    assertEquals("rules", location.currentDocumentId)
    assertFalse(location.currentInBaseDir)
  }

  @Test
  fun leavesDirectoriesUntilBaseDirectory() {
    val location =
      ProfileFilesLocation()
        .initialize("root-profile")
        .enterDirectory("providers")
        .enterDirectory("rules")

    val parent = checkNotNull(location.leaveDirectory())
    val root = checkNotNull(parent.leaveDirectory())

    assertEquals("providers", parent.currentDocumentId)
    assertFalse(parent.currentInBaseDir)
    assertEquals("root-profile", root.currentDocumentId)
    assertTrue(root.currentInBaseDir)
    assertNull(root.leaveDirectory())
  }
}
