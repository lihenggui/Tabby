package com.github.kr328.clash.common.document

fun tabbyDocumentOpenModeRequestsWrite(mode: String?): Boolean {
  return mode?.contains("w", ignoreCase = true) ?: true
}

fun tabbyProfileConfigurationDocumentAllowsWritableOpen(profileIsFile: Boolean): Boolean {
  return profileIsFile
}

fun tabbyProfileConfigurationDocumentFlags(profileIsUrl: Boolean): Set<Flag> {
  return if (profileIsUrl) emptySet() else setOf(Flag.Writable)
}

fun tabbyDocumentIdIsChild(parentDocumentId: String?, documentId: String?): Boolean {
  if (parentDocumentId == null || documentId == null) return false

  return documentId.startsWith(parentDocumentId)
}
