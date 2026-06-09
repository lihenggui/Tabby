package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileFileExportActionTest {
  @Test
  fun ignoresMissingExportOutput() {
    assertEquals(
      ProfileFileExportAction.Ignore,
      profileFileExportAction(outputSelected = false, sourceDocumentId = "root/config.yaml"),
    )
  }

  @Test
  fun resolvedExportIgnoresMissingOutput() {
    assertEquals(
      ProfileFileExportResolvedAction.Ignore,
      profileFileExportResolvedAction<String>(
        output = null,
        sourceDocumentId = "root/config.yaml",
      ),
    )
  }

  @Test
  fun ignoresMissingSourceFile() {
    assertEquals(
      ProfileFileExportAction.Ignore,
      profileFileExportAction(outputSelected = true, sourceDocumentId = null),
    )
  }

  @Test
  fun resolvedExportIgnoresMissingSourceFile() {
    assertEquals(
      ProfileFileExportResolvedAction.Ignore,
      profileFileExportResolvedAction(
        output = "content://output/config.yaml",
        sourceDocumentId = null,
      ),
    )
  }

  @Test
  fun exportsSelectedSourceFiles() {
    assertEquals(
      ProfileFileExportAction.ExportFile("root/config.yaml"),
      profileFileExportAction(outputSelected = true, sourceDocumentId = "root/config.yaml"),
    )
  }

  @Test
  fun resolvedExportPairsSelectedOutputWithSourceDocument() {
    assertEquals(
      ProfileFileExportResolvedAction.ExportFile(
        output = "content://output/config.yaml",
        sourceDocumentId = "root/config.yaml",
      ),
      profileFileExportResolvedAction(
        output = "content://output/config.yaml",
        sourceDocumentId = "root/config.yaml",
      ),
    )
  }
}
