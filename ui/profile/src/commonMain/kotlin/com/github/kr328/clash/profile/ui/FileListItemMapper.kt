package com.github.kr328.clash.profile.ui

internal fun toFileListItem(
  id: String,
  name: String,
  sizeBytes: Long,
  lastModified: Long,
  isDirectory: Boolean,
  currentTime: Long,
  formatBytes: (Long) -> String,
  formatElapsedMillis: (Long) -> String,
): FileListItem {
  return FileListItem(
    id = id,
    name = name,
    sizeBytes = sizeBytes,
    isDirectory = isDirectory,
    sizeText = if (isDirectory) null else formatBytes(sizeBytes),
    updatedAtText =
      if (isDirectory) {
        null
      } else {
        formatElapsedMillis(currentTime - lastModified)
      },
  )
}
