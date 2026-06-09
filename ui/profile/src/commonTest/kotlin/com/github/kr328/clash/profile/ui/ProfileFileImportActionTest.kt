package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileFileImportActionTest {
  @Test
  fun importResultKeepsSelectedSourceFileNameAndTarget() {
    assertEquals(
      ProfileFilesImportResult(
        source = "content://source/provider.yaml",
        sourceFileName = "provider.yaml",
        targetDocumentId = "root/config.yaml",
      ),
      profileFilesImportResult(
        source = "content://source/provider.yaml",
        sourceFileName = { "provider.yaml" },
        targetDocumentId = "root/config.yaml",
      ),
    )
  }

  @Test
  fun importResultDropsMissingSourceFieldsAndSkipsFileName() {
    var fileNameLoaded = false

    assertEquals(
      ProfileFilesImportResult<String>(
        source = null,
        sourceFileName = null,
        targetDocumentId = null,
      ),
      profileFilesImportResult(
        source = null,
        sourceFileName = {
          fileNameLoaded = true
          "ignored.yaml"
        },
        targetDocumentId = "root/config.yaml",
      ),
    )
    assertEquals(false, fileNameLoaded)
  }

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
  }

  @Test
  fun resolvedImportIgnoresMissingSource() {
    assertEquals(
      ProfileFileImportResolvedAction.Ignore,
      profileFileImportResolvedAction<String>(
        source = null,
        sourceFileName = "ignored.yaml",
        targetDocumentId = null,
        parentDocumentId = "root",
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
  }

  @Test
  fun resolvedImportPairsSelectedSourceWithImportAction() {
    assertEquals(
      ProfileFileImportResolvedAction.ImportNewFile(
        source = "content://source/provider.yaml",
        parentDocumentId = "root/providers",
        fileName = "provider.yaml",
      ),
      profileFileImportResolvedAction(
        source = "content://source/provider.yaml",
        sourceFileName = "provider.yaml",
        targetDocumentId = null,
        parentDocumentId = "root/providers",
      ),
    )
    assertEquals(
      ProfileFileImportResolvedAction.ImportNewFile(
        source = "content://source/unknown",
        parentDocumentId = "root",
        fileName = "File",
      ),
      profileFileImportResolvedAction(
        source = "content://source/unknown",
        sourceFileName = null,
        targetDocumentId = null,
        parentDocumentId = "root",
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
  }

  @Test
  fun resolvedImportPairsSelectedSourceWithReplaceAction() {
    assertEquals(
      ProfileFileImportResolvedAction.ReplaceFile(
        source = "content://source/config.yaml",
        targetDocumentId = "root/config.yaml",
      ),
      profileFileImportResolvedAction(
        source = "content://source/config.yaml",
        sourceFileName = "ignored.yaml",
        targetDocumentId = "root/config.yaml",
        parentDocumentId = "root",
      ),
    )
  }
}
