package com.github.kr328.clash.profile.ui

internal fun <T> toFileListItems(
  files: List<T>,
  currentTime: Long,
  id: (T) -> String,
  name: (T) -> String,
  sizeBytes: (T) -> Long,
  lastModified: (T) -> Long,
  isDirectory: (T) -> Boolean,
  formatBytes: (Long) -> String,
  formatElapsedMillis: (Long) -> String,
): List<FileListItem> {
  return files.map { file ->
    toFileListItem(
      id = id(file),
      name = name(file),
      sizeBytes = sizeBytes(file),
      lastModified = lastModified(file),
      isDirectory = isDirectory(file),
      currentTime = currentTime,
      formatBytes = formatBytes,
      formatElapsedMillis = formatElapsedMillis,
    )
  }
}

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

internal sealed interface ProfileFileListItemSelection<out T> {
  data class Select<T>(val file: T) : ProfileFileListItemSelection<T>

  data object Ignore : ProfileFileListItemSelection<Nothing>
}

internal fun <T> profileFileListItemSelection(
  item: FileListItem,
  fileById: Map<String, T>,
): ProfileFileListItemSelection<T> {
  val file = fileById[item.id] ?: return ProfileFileListItemSelection.Ignore
  return ProfileFileListItemSelection.Select(file)
}
