package com.github.kr328.clash.profile.ui

internal sealed interface ProfileFileExportAction {
  data class ExportFile(val sourceDocumentId: String) : ProfileFileExportAction

  data object Ignore : ProfileFileExportAction
}

internal data class ProfileFileExportResult(
  val outputSelected: Boolean,
  val sourceDocumentId: String?,
)

internal fun profileFileExportResultFromPlatformPayload(
  outputSelected: Boolean,
  sourceDocumentId: String?,
): ProfileFileExportResult {
  return ProfileFileExportResult(
    outputSelected = outputSelected,
    sourceDocumentId = if (outputSelected) sourceDocumentId else null,
  )
}

internal fun profileFileExportAction(result: ProfileFileExportResult): ProfileFileExportAction {
  return profileFileExportAction(
    outputSelected = result.outputSelected,
    sourceDocumentId = result.sourceDocumentId,
  )
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
