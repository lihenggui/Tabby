package com.github.kr328.clash.profile.ui

internal sealed interface ProfileFileExportAction {
  data class ExportFile(val sourceDocumentId: String) : ProfileFileExportAction

  data object Ignore : ProfileFileExportAction
}

internal fun profileFileExportAction(
  outputSelected: Boolean,
  sourceDocumentId: String?,
): ProfileFileExportAction {
  return if (outputSelected && sourceDocumentId != null) {
    ProfileFileExportAction.ExportFile(sourceDocumentId)
  } else {
    ProfileFileExportAction.Ignore
  }
}
