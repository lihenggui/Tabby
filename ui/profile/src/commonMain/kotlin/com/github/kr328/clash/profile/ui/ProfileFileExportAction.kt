package com.github.kr328.clash.profile.ui

internal sealed interface ProfileFileExportAction {
  data class ExportFile(val sourceDocumentId: String) : ProfileFileExportAction

  data object Ignore : ProfileFileExportAction
}

internal sealed interface ProfileFileExportResolvedAction<out OutputT : Any> {
  data class ExportFile<out OutputT : Any>(
    val output: OutputT,
    val sourceDocumentId: String,
  ) : ProfileFileExportResolvedAction<OutputT>

  data object Ignore : ProfileFileExportResolvedAction<Nothing>
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

internal fun <OutputT : Any> profileFileExportResolvedAction(
  output: OutputT?,
  sourceDocumentId: String?,
): ProfileFileExportResolvedAction<OutputT> {
  val selectedOutput = output ?: return ProfileFileExportResolvedAction.Ignore

  return when (
    val action =
      profileFileExportAction(
        outputSelected = true,
        sourceDocumentId = sourceDocumentId,
      )
  ) {
    is ProfileFileExportAction.ExportFile ->
      ProfileFileExportResolvedAction.ExportFile(
        output = selectedOutput,
        sourceDocumentId = action.sourceDocumentId,
      )
    ProfileFileExportAction.Ignore -> ProfileFileExportResolvedAction.Ignore
  }
}
