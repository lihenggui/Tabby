package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileFilesDocumentTest {
  @Test
  fun mapsDocumentToRouteItem() {
    assertEquals(
      ProfileFileRouteItem(
        id = "profile/config.yaml",
        name = "config.yaml",
        sizeBytes = 128,
        lastModified = 42,
        isDirectory = false,
      ),
      ProfileFilesDocument(
          id = "profile/config.yaml",
          name = "config.yaml",
          sizeBytes = 128,
          lastModified = 42,
          isDirectory = false,
        )
        .toProfileFileRouteItem(),
    )
  }
}
