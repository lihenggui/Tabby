package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileFileExportActionTest {
  @Test
  fun platformPayloadCreatesExportResultWithSourceDocument() {
    assertEquals(
      ProfileFileExportResult(
        outputSelected = true,
        sourceDocumentId = "root/config.yaml",
      ),
      profileFileExportResultFromPlatformPayload(
        outputSelected = true,
        sourceDocumentId = "root/config.yaml",
      ),
    )
  }

  @Test
  fun platformPayloadDropsExportSourceDocumentWhenOutputIsMissing() {
    assertEquals(
      ProfileFileExportResult(
        outputSelected = false,
        sourceDocumentId = null,
      ),
      profileFileExportResultFromPlatformPayload(
        outputSelected = false,
        sourceDocumentId = "root/config.yaml",
      ),
    )
  }

  @Test
  fun resolvedPlatformPayloadKeepsSourceDocumentOnlyWhenOutputExists() {
    assertEquals(
      ProfileFileExportResolvedResult(
        output = "content://output/config.yaml",
        sourceDocumentId = "root/config.yaml",
      ),
      profileFileExportResolvedResultFromPlatformPayload(
        output = "content://output/config.yaml",
        sourceDocumentId = "root/config.yaml",
      ),
    )
    assertEquals(
      ProfileFileExportResolvedResult(
        output = null,
        sourceDocumentId = null,
      ),
      profileFileExportResolvedResultFromPlatformPayload<String>(
        output = null,
        sourceDocumentId = "root/config.yaml",
      ),
    )
  }

  @Test
  fun ignoresMissingExportOutput() {
    assertEquals(
      ProfileFileExportAction.Ignore,
      profileFileExportAction(outputSelected = false, sourceDocumentId = "root/config.yaml"),
    )
    assertEquals(
      ProfileFileExportAction.Ignore,
      profileFileExportAction(
        ProfileFileExportResult(
          outputSelected = false,
          sourceDocumentId = "root/config.yaml",
        )
      ),
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
    assertEquals(
      ProfileFileExportResolvedAction.Ignore,
      profileFileExportResolvedAction(
        ProfileFileExportResolvedResult(
          output = null,
          sourceDocumentId = "root/config.yaml",
        )
      ),
    )
  }

  @Test
  fun ignoresMissingSourceFile() {
    assertEquals(
      ProfileFileExportAction.Ignore,
      profileFileExportAction(outputSelected = true, sourceDocumentId = null),
    )
    assertEquals(
      ProfileFileExportAction.Ignore,
      profileFileExportAction(
        ProfileFileExportResult(
          outputSelected = true,
          sourceDocumentId = null,
        )
      ),
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
    assertEquals(
      ProfileFileExportResolvedAction.Ignore,
      profileFileExportResolvedAction(
        ProfileFileExportResolvedResult(
          output = "content://output/config.yaml",
          sourceDocumentId = null,
        )
      ),
    )
  }

  @Test
  fun exportsSelectedSourceFiles() {
    assertEquals(
      ProfileFileExportAction.ExportFile("root/config.yaml"),
      profileFileExportAction(outputSelected = true, sourceDocumentId = "root/config.yaml"),
    )
    assertEquals(
      ProfileFileExportAction.ExportFile("root/config.yaml"),
      profileFileExportAction(
        ProfileFileExportResult(
          outputSelected = true,
          sourceDocumentId = "root/config.yaml",
        )
      ),
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
    assertEquals(
      ProfileFileExportResolvedAction.ExportFile(
        output = "content://output/config.yaml",
        sourceDocumentId = "root/config.yaml",
      ),
      profileFileExportResolvedAction(
        ProfileFileExportResolvedResult(
          output = "content://output/config.yaml",
          sourceDocumentId = "root/config.yaml",
        )
      ),
    )
  }
}
