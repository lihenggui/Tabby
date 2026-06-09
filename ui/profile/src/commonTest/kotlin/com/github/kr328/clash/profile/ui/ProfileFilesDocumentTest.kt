package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileFilesDocumentTest {
  @Test
  fun mapsPlatformPayloadToDocument() {
    assertEquals(
      ProfileFilesDocument(
        id = "profile/providers/provider.yaml",
        name = "provider.yaml",
        sizeBytes = 256,
        lastModified = 84,
        isDirectory = false,
      ),
      profileFilesDocumentFromPlatformPayload(
        document =
          TestPlatformDocument(
            id = "profile/providers/provider.yaml",
            name = "provider.yaml",
            size = 256,
            lastModified = 84,
            directory = false,
          ),
        id = TestPlatformDocument::id,
        name = TestPlatformDocument::name,
        sizeBytes = TestPlatformDocument::size,
        lastModified = TestPlatformDocument::lastModified,
        isDirectory = TestPlatformDocument::directory,
      ),
    )
  }

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

  private data class TestPlatformDocument(
    val id: String,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val directory: Boolean,
  )
}
