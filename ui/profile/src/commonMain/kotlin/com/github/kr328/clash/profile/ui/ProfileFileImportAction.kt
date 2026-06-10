package com.github.kr328.clash.profile.ui

internal sealed interface ProfileFileImportAction {
  data class ImportNewFile(
    val parentDocumentId: String,
    val fileName: String,
  ) : ProfileFileImportAction

  data class ReplaceFile(val targetDocumentId: String) : ProfileFileImportAction

  data object Ignore : ProfileFileImportAction
}

internal sealed interface ProfileFileImportResolvedAction<out SourceT : Any> {
  data class ImportNewFile<out SourceT : Any>(
    val source: SourceT,
    val parentDocumentId: String,
    val fileName: String,
  ) : ProfileFileImportResolvedAction<SourceT>

  data class ReplaceFile<out SourceT : Any>(
    val source: SourceT,
    val targetDocumentId: String,
  ) : ProfileFileImportResolvedAction<SourceT>

  data object Ignore : ProfileFileImportResolvedAction<Nothing>
}

internal fun profileFileImportAction(
  sourceSelected: Boolean,
  sourceFileName: String?,
  targetDocumentId: String?,
  parentDocumentId: String,
): ProfileFileImportAction {
  if (!sourceSelected) return ProfileFileImportAction.Ignore

  return if (targetDocumentId == null) {
    ProfileFileImportAction.ImportNewFile(
      parentDocumentId = parentDocumentId,
      fileName = sourceFileName ?: DEFAULT_IMPORTED_PROFILE_FILE_NAME,
    )
  } else {
    ProfileFileImportAction.ReplaceFile(targetDocumentId)
  }
}

internal fun <SourceT : Any> profileFileImportResolvedAction(
  source: SourceT?,
  sourceFileName: String?,
  targetDocumentId: String?,
  parentDocumentId: String,
): ProfileFileImportResolvedAction<SourceT> {
  val selectedSource = source ?: return ProfileFileImportResolvedAction.Ignore

  return when (
    val action =
      profileFileImportAction(
        sourceSelected = true,
        sourceFileName = sourceFileName,
        targetDocumentId = targetDocumentId,
        parentDocumentId = parentDocumentId,
      )
  ) {
    is ProfileFileImportAction.ImportNewFile ->
      ProfileFileImportResolvedAction.ImportNewFile(
        source = selectedSource,
        parentDocumentId = action.parentDocumentId,
        fileName = action.fileName,
      )
    is ProfileFileImportAction.ReplaceFile ->
      ProfileFileImportResolvedAction.ReplaceFile(
        source = selectedSource,
        targetDocumentId = action.targetDocumentId,
      )
    ProfileFileImportAction.Ignore -> ProfileFileImportResolvedAction.Ignore
  }
}

private const val DEFAULT_IMPORTED_PROFILE_FILE_NAME = "File"
