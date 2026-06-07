package com.github.kr328.clash.profile.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

data class ProfileFileRouteItem(
  val id: String,
  val name: String,
  val sizeBytes: Long,
  val lastModified: Long,
  val isDirectory: Boolean,
)

@Composable
fun FilesRouteContent(
  modifier: Modifier = Modifier,
  files: List<ProfileFileRouteItem> = emptyList(),
  currentTimeMillis: Long = 0,
  currentInBaseDir: Boolean = true,
  configurationEditable: Boolean = false,
  formatBytes: (Long) -> String = ::profileBinaryBytesText,
  formatElapsedMillis: @Composable (Long) -> String = { elapsedTimeTextString(it) },
  onBack: () -> Unit = {},
  onOpen: (ProfileFileRouteItem) -> Unit = {},
  onNew: () -> Unit = {},
  onImport: (ProfileFileRouteItem) -> Unit = {},
  onExport: (ProfileFileRouteItem) -> Unit = {},
  onRename: (ProfileFileRouteItem, String) -> Unit = { _, _ -> },
  onDelete: (ProfileFileRouteItem) -> Unit = {},
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val fileById = remember(files) { files.associateBy(ProfileFileRouteItem::id) }
  val effectiveCurrentTime =
    maxOf(currentTimeMillis, files.maxOfOrNull { it.lastModified } ?: currentTimeMillis)

  fun handleSelectedFile(item: FileListItem, action: (ProfileFileRouteItem) -> Unit) {
    when (val selection = profileFileListItemSelection(item, fileById)) {
      is ProfileFileListItemSelection.Select -> action(selection.file)
      ProfileFileListItemSelection.Ignore -> Unit
    }
  }

  FilesContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    files =
      files.toProfileFileRouteListItems(
        currentTimeMillis = effectiveCurrentTime,
        formatBytes = formatBytes,
        formatElapsedMillis = formatElapsedMillis,
      ),
    currentInBaseDir = currentInBaseDir,
    configurationEditable = configurationEditable,
    onBack = onBack,
    onOpen = { item -> handleSelectedFile(item, onOpen) },
    onNew = onNew,
    onImport = { item -> handleSelectedFile(item, onImport) },
    onExport = { item -> handleSelectedFile(item, onExport) },
    onRename = { item, name -> handleSelectedFile(item) { file -> onRename(file, name) } },
    onDelete = { item -> handleSelectedFile(item, onDelete) },
  )
}

@Composable
private fun List<ProfileFileRouteItem>.toProfileFileRouteListItems(
  currentTimeMillis: Long,
  formatBytes: (Long) -> String,
  formatElapsedMillis: @Composable (Long) -> String,
): List<FileListItem> {
  val items = ArrayList<FileListItem>(size)

  for (file in this) {
    items +=
      FileListItem(
        id = file.id,
        name = file.name,
        sizeBytes = file.sizeBytes,
        isDirectory = file.isDirectory,
        sizeText = if (file.isDirectory) null else formatBytes(file.sizeBytes),
        updatedAtText =
          if (file.isDirectory) {
            null
          } else {
            formatElapsedMillis(currentTimeMillis - file.lastModified)
          },
      )
  }

  return items
}
