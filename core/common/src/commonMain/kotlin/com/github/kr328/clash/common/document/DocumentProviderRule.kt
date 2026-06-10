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

fun tabbyVirtualDirectoryDocumentFlags(): Set<Flag> {
  return setOf(Flag.Virtual)
}

fun tabbyProviderFileDocumentFlags(): Set<Flag> {
  return setOf(Flag.Writable, Flag.Deletable)
}

const val TABBY_TEXT_DOCUMENT_MIME_TYPE = "text/plain"

fun tabbyFileDocumentMimeType(isDirectory: Boolean, directoryMimeType: String): String {
  return if (isDirectory) directoryMimeType else TABBY_TEXT_DOCUMENT_MIME_TYPE
}

fun tabbyDocumentProviderRootFlags(localOnlyFlag: Int, supportsIsChildFlag: Int): Int {
  return localOnlyFlag or supportsIsChildFlag
}

data class TabbyDocumentListSortKey(val directoryRank: Int, val name: String) :
  Comparable<TabbyDocumentListSortKey> {
  override fun compareTo(other: TabbyDocumentListSortKey): Int {
    return compareValuesBy(
      this,
      other,
      TabbyDocumentListSortKey::directoryRank,
      TabbyDocumentListSortKey::name,
    )
  }
}

fun tabbyDocumentListSortKey(isDirectory: Boolean, name: String): TabbyDocumentListSortKey {
  return TabbyDocumentListSortKey(directoryRank = if (isDirectory) 0 else 1, name = name)
}

fun tabbyDocumentIdIsChild(parentDocumentId: String?, documentId: String?): Boolean {
  if (parentDocumentId == null || documentId == null) return false

  return documentId.startsWith(parentDocumentId)
}
