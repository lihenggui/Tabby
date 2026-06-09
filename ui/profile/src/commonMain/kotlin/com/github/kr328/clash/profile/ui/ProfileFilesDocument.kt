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

internal fun <T> profileFilesDocumentFromPlatformPayload(
  document: T,
  id: (T) -> String,
  name: (T) -> String,
  sizeBytes: (T) -> Long,
  lastModified: (T) -> Long,
  isDirectory: (T) -> Boolean,
): ProfileFilesDocument {
  return ProfileFilesDocument(
    id = id(document),
    name = name(document),
    sizeBytes = sizeBytes(document),
    lastModified = lastModified(document),
    isDirectory = isDirectory(document),
  )
}

internal fun <SourceT : Any> profileFilesImportResult(
  source: SourceT?,
  sourceFileName: (SourceT) -> String?,
  targetDocumentId: String?,
): ProfileFilesImportResult<SourceT> {
  val selectedSource = source

  return ProfileFilesImportResult(
    source = selectedSource,
    sourceFileName = if (selectedSource != null) sourceFileName(selectedSource) else null,
    targetDocumentId = if (selectedSource != null) targetDocumentId else null,
  )
}

internal fun <OutputT : Any> profileFilesExportResult(
  output: OutputT?,
  sourceDocumentId: String?,
): ProfileFilesExportResult<OutputT> {
  val selectedOutput = output

  return ProfileFilesExportResult(
    output = selectedOutput,
    sourceDocumentId = if (selectedOutput != null) sourceDocumentId else null,
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
