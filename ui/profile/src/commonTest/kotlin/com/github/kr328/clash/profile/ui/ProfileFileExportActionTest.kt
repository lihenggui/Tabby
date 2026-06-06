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
  fun ignoresMissingSourceFile() {
    assertEquals(
      ProfileFileExportAction.Ignore,
      profileFileExportAction(outputSelected = true, sourceDocumentId = null),
    )
  }

  @Test
  fun exportsSelectedSourceFiles() {
    assertEquals(
      ProfileFileExportAction.ExportFile("root/config.yaml"),
      profileFileExportAction(outputSelected = true, sourceDocumentId = "root/config.yaml"),
    )
  }
}
