package com.github.kr328.clash.profile.ui

data class ProfileFilesDocument(
  val id: String,
  val name: String,
  val sizeBytes: Long,
  val lastModified: Long,
  val isDirectory: Boolean,
)

interface ProfileFilesDocumentClient<in SourceT : Any, in OutputT : Any> {
  suspend fun list(parentDocumentId: String): List<ProfileFilesDocument>

  suspend fun renameDocument(documentId: String, name: String)

  suspend fun deleteDocument(documentId: String)

  suspend fun importDocument(parentDocumentId: String, source: SourceT, name: String)

  suspend fun replaceDocument(documentId: String, source: SourceT)

  suspend fun exportDocument(output: OutputT, documentId: String)
}

data class ProfileFilesImportResult<out SourceT : Any>(
  val source: SourceT?,
  val sourceFileName: String?,
  val targetDocumentId: String?,
)

data class ProfileFilesExportResult<out OutputT : Any>(
  val output: OutputT?,
  val sourceDocumentId: String?,
)

internal fun <SourceT : Any> profileFilesImportResultFromPlatformPayload(
  source: SourceT?,
  sourceFileName: String?,
  pendingTargetDocument: ProfileFilesDocument?,
): ProfileFilesImportResult<SourceT> {
  return ProfileFilesImportResult(
    source = source,
    sourceFileName = if (source != null) sourceFileName else null,
    targetDocumentId = if (source != null) pendingTargetDocument?.id else null,
  )
}

internal fun <OutputT : Any> profileFilesExportResultFromPlatformPayload(
  output: OutputT?,
  pendingSourceDocument: ProfileFilesDocument?,
): ProfileFilesExportResult<OutputT> {
  return ProfileFilesExportResult(
    output = output,
    sourceDocumentId = if (output != null) pendingSourceDocument?.id else null,
  )
}

internal fun profileFilesDocumentFromPlatformPayload(
  id: String,
  name: String,
  sizeBytes: Long,
  lastModified: Long,
  isDirectory: Boolean,
): ProfileFilesDocument {
  return ProfileFilesDocument(
    id = id,
    name = name,
    sizeBytes = sizeBytes,
    lastModified = lastModified,
    isDirectory = isDirectory,
  )
}

internal fun ProfileFilesDocument.toProfileFileRouteItem(): ProfileFileRouteItem {
  return ProfileFileRouteItem(
    id = id,
    name = name,
    sizeBytes = sizeBytes,
    lastModified = lastModified,
    isDirectory = isDirectory,
  )
}
