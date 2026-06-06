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
}
