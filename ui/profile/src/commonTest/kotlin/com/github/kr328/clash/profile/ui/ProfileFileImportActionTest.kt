package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileFileImportActionTest {
  @Test
  fun ignoresMissingImportSource() {
    assertEquals(
      ProfileFileImportAction.Ignore,
      profileFileImportAction(
        sourceSelected = false,
        sourceFileName = "ignored.yaml",
        targetDocumentId = null,
        parentDocumentId = "root",
      ),
    )
    assertEquals(
      ProfileFileImportAction.Ignore,
      profileFileImportAction(
        ProfileFileImportResult(
          sourceSelected = false,
          sourceFileName = "ignored.yaml",
          targetDocumentId = null,
          parentDocumentId = "root",
        )
      ),
    )
  }

  @Test
  fun importsNewFilesIntoCurrentDirectory() {
    assertEquals(
      ProfileFileImportAction.ImportNewFile(
        parentDocumentId = "root/providers",
        fileName = "provider.yaml",
      ),
      profileFileImportAction(
        sourceSelected = true,
        sourceFileName = "provider.yaml",
        targetDocumentId = null,
        parentDocumentId = "root/providers",
      ),
    )
    assertEquals(
      ProfileFileImportAction.ImportNewFile(
        parentDocumentId = "root/providers",
        fileName = "provider.yaml",
      ),
      profileFileImportAction(
        ProfileFileImportResult(
          sourceSelected = true,
          sourceFileName = "provider.yaml",
          targetDocumentId = null,
          parentDocumentId = "root/providers",
        )
      ),
    )
  }

  @Test
  fun importsNewFilesWithFallbackFileName() {
    assertEquals(
      ProfileFileImportAction.ImportNewFile(parentDocumentId = "root", fileName = "File"),
      profileFileImportAction(
        sourceSelected = true,
        sourceFileName = null,
        targetDocumentId = null,
        parentDocumentId = "root",
      ),
    )
    assertEquals(
      ProfileFileImportAction.ImportNewFile(parentDocumentId = "root", fileName = "File"),
      profileFileImportAction(
        ProfileFileImportResult(
          sourceSelected = true,
          sourceFileName = null,
          targetDocumentId = null,
          parentDocumentId = "root",
        )
      ),
    )
  }

  @Test
  fun replacesTargetFiles() {
    assertEquals(
      ProfileFileImportAction.ReplaceFile("root/config.yaml"),
      profileFileImportAction(
        sourceSelected = true,
        sourceFileName = "ignored.yaml",
        targetDocumentId = "root/config.yaml",
        parentDocumentId = "root",
      ),
    )
    assertEquals(
      ProfileFileImportAction.ReplaceFile("root/config.yaml"),
      profileFileImportAction(
        ProfileFileImportResult(
          sourceSelected = true,
          sourceFileName = "ignored.yaml",
          targetDocumentId = "root/config.yaml",
          parentDocumentId = "root",
        )
      ),
    )
  }
}
