package com.github.kr328.clash.profile.ui

internal sealed interface ProfileFileImportAction {
  data class ImportNewFile(
    val parentDocumentId: String,
    val fileName: String,
  ) : ProfileFileImportAction

  data class ReplaceFile(val targetDocumentId: String) : ProfileFileImportAction

  data object Ignore : ProfileFileImportAction
}

internal data class ProfileFileImportResult(
  val sourceSelected: Boolean,
  val sourceFileName: String?,
  val targetDocumentId: String?,
  val parentDocumentId: String,
)

internal fun profileFileImportResultFromPlatformPayload(
  sourceSelected: Boolean,
  sourceFileName: String?,
  targetDocumentId: String?,
  parentDocumentId: String,
): ProfileFileImportResult {
  return ProfileFileImportResult(
    sourceSelected = sourceSelected,
    sourceFileName = if (sourceSelected) sourceFileName else null,
    targetDocumentId = targetDocumentId,
    parentDocumentId = parentDocumentId,
  )
}

internal fun profileFileImportAction(result: ProfileFileImportResult): ProfileFileImportAction {
  return profileFileImportAction(
    sourceSelected = result.sourceSelected,
    sourceFileName = result.sourceFileName,
    targetDocumentId = result.targetDocumentId,
    parentDocumentId = result.parentDocumentId,
  )
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

private const val DEFAULT_IMPORTED_PROFILE_FILE_NAME = "File"
