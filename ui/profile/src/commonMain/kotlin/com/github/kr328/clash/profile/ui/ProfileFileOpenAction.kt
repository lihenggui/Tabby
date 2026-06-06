package com.github.kr328.clash.profile.ui

internal sealed interface ProfileFileOpenAction {
  data class EnterDirectory(val documentId: String) : ProfileFileOpenAction

  data class OpenFile(val documentId: String) : ProfileFileOpenAction
}

internal fun profileFileOpenAction(
  documentId: String,
  isDirectory: Boolean,
): ProfileFileOpenAction {
  return if (isDirectory) ProfileFileOpenAction.EnterDirectory(documentId)
  else ProfileFileOpenAction.OpenFile(documentId)
}
