package com.github.kr328.clash.profile.ui

internal data class ProfileFilesLocation(
  val rootDocumentId: String = "",
  val directoryStack: List<String> = emptyList(),
) {
  val initialized: Boolean
    get() = rootDocumentId.isNotEmpty()

  val currentDocumentId: String
    get() = directoryStack.lastOrNull() ?: rootDocumentId

  val currentInBaseDir: Boolean
    get() = directoryStack.isEmpty()

  fun initialize(rootDocumentId: String): ProfileFilesLocation {
    return if (initialized) this else copy(rootDocumentId = rootDocumentId)
  }

  fun enterDirectory(documentId: String): ProfileFilesLocation {
    return copy(directoryStack = directoryStack + documentId)
  }

  fun leaveDirectory(): ProfileFilesLocation? {
    return if (directoryStack.isEmpty()) null else copy(directoryStack = directoryStack.dropLast(1))
  }
}
