package com.github.kr328.clash.common.document

fun tabbyDocumentOpenModeRequestsWrite(mode: String?): Boolean {
  return mode?.contains("w", ignoreCase = true) ?: true
}

fun tabbyDocumentIdIsChild(parentDocumentId: String?, documentId: String?): Boolean {
  if (parentDocumentId == null || documentId == null) return false

  return documentId.startsWith(parentDocumentId)
}
