package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileFilesDocumentTest {
  @Test
  fun mapsImportPlatformPayloadToRouteResultWhenSourceExists() {
    val target = profileFilesDocument(id = "profile/config.yaml")

    assertEquals(
      ProfileFilesImportResult(
        source = "content://source/provider.yaml",
        sourceFileName = "provider.yaml",
        targetDocumentId = "profile/config.yaml",
      ),
      profileFilesImportResultFromPlatformPayload(
        source = "content://source/provider.yaml",
        sourceFileName = "provider.yaml",
        pendingTargetDocument = target,
      ),
    )
  }

  @Test
  fun importPlatformPayloadDropsMetadataWhenSourceIsMissing() {
    val target = profileFilesDocument(id = "profile/config.yaml")

    assertEquals(
      ProfileFilesImportResult<String>(
        source = null,
        sourceFileName = null,
        targetDocumentId = null,
      ),
      profileFilesImportResultFromPlatformPayload(
        source = null,
        sourceFileName = "ignored.yaml",
        pendingTargetDocument = target,
      ),
    )
  }

  @Test
  fun mapsExportPlatformPayloadToRouteResultWhenOutputExists() {
    val source = profileFilesDocument(id = "profile/config.yaml")

    assertEquals(
      ProfileFilesExportResult(
        output = "content://output/config.yaml",
        sourceDocumentId = "profile/config.yaml",
      ),
      profileFilesExportResultFromPlatformPayload(
        output = "content://output/config.yaml",
        pendingSourceDocument = source,
      ),
    )
  }

  @Test
  fun exportPlatformPayloadDropsSourceDocumentWhenOutputIsMissing() {
    val source = profileFilesDocument(id = "profile/config.yaml")

    assertEquals(
      ProfileFilesExportResult<String>(
        output = null,
        sourceDocumentId = null,
      ),
      profileFilesExportResultFromPlatformPayload(
        output = null,
        pendingSourceDocument = source,
      ),
    )
  }

  @Test
  fun mapsPlatformPayloadToDocument() {
    assertEquals(
      ProfileFilesDocument(
        id = "profile/config.yaml",
        name = "config.yaml",
        sizeBytes = 128,
        lastModified = 42,
        isDirectory = false,
      ),
      profileFilesDocumentFromPlatformPayload(
        id = "profile/config.yaml",
        name = "config.yaml",
        sizeBytes = 128,
        lastModified = 42,
        isDirectory = false,
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

  private fun profileFilesDocument(id: String): ProfileFilesDocument {
    return ProfileFilesDocument(
      id = id,
      name = id.substringAfterLast('/'),
      sizeBytes = 128,
      lastModified = 42,
      isDirectory = false,
    )
  }
}
