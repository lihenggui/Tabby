package com.github.kr328.clash.profile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.common.util.tabbyIsValidFileNameInput
import com.github.kr328.clash.ui.component.SizeSpacer
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineAdd
import com.github.kr328.clash.ui.icon.BaselineEdit
import com.github.kr328.clash.ui.icon.BaselineGetApp
import com.github.kr328.clash.ui.icon.BaselineMoreVert
import com.github.kr328.clash.ui.icon.BaselineSave
import com.github.kr328.clash.ui.icon.OutlineArticle
import com.github.kr328.clash.ui.icon.OutlineDelete
import com.github.kr328.clash.ui.icon.OutlineFolder
import com.github.kr328.clash.ui.icon.TabbyIcons
import org.jetbrains.compose.resources.stringResource
import tabby.ui.profile.generated.resources.Res as ProfileRes
import tabby.ui.profile.generated.resources.file_name
import tabby.ui.profile.generated.resources.files
import tabby.ui.profile.generated.resources.import_
import tabby.ui.profile.generated.resources.invalid_file_name
import tabby.ui.profile.generated.resources.rename
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources._new
import tabby.ui.shared.generated.resources.cancel
import tabby.ui.shared.generated.resources.delete
import tabby.ui.shared.generated.resources.export
import tabby.ui.shared.generated.resources.more
import tabby.ui.shared.generated.resources.ok

internal data class FileListItem(
  val id: String,
  val name: String,
  val sizeBytes: Long,
  val isDirectory: Boolean,
  val sizeText: String?,
  val updatedAtText: String?,
)

@Composable
internal fun FilesContent(
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState,
  files: List<FileListItem>,
  currentInBaseDir: Boolean,
  configurationEditable: Boolean,
  onBack: () -> Unit,
  onOpen: (FileListItem) -> Unit,
  onNew: () -> Unit,
  onImport: (FileListItem) -> Unit,
  onExport: (FileListItem) -> Unit,
  onRename: (FileListItem, String) -> Unit,
  onDelete: (FileListItem) -> Unit,
) {
  var menuFile by remember { mutableStateOf<FileListItem?>(null) }
  var renameFile by remember { mutableStateOf<FileListItem?>(null) }

  menuFile?.let { file ->
    ModalBottomSheet(
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
      onDismissRequest = { menuFile = null },
    ) {
      if (!file.isDirectory && (!currentInBaseDir || configurationEditable)) {
        FilesMenuAction(
          icon = TabbyIcons.BaselineGetApp,
          text = stringResource(ProfileRes.string.import_),
          onClick = {
            menuFile = null
            onImport(file)
          },
        )
      }
      if (!file.isDirectory && file.sizeBytes > 0) {
        FilesMenuAction(
          icon = TabbyIcons.BaselineSave,
          text = stringResource(SharedRes.string.export),
          onClick = {
            menuFile = null
            onExport(file)
          },
        )
      }
      if (!currentInBaseDir) {
        FilesMenuAction(
          icon = TabbyIcons.BaselineEdit,
          text = stringResource(ProfileRes.string.rename),
          onClick = {
            menuFile = null
            renameFile = file
          },
        )
        FilesMenuAction(
          icon = TabbyIcons.OutlineDelete,
          text = stringResource(SharedRes.string.delete),
          tint = MaterialTheme.colorScheme.error,
          onClick = {
            menuFile = null
            onDelete(file)
          },
        )
      }
      SizeSpacer(16.dp)
    }
  }

  TabbyScaffold(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    title = stringResource(ProfileRes.string.files),
    onBack = onBack,
    actions = {
      if (!currentInBaseDir) {
        IconButton(onClick = onNew) {
          Icon(
            imageVector = TabbyIcons.BaselineAdd,
            contentDescription = stringResource(SharedRes.string._new),
          )
        }
      }
    },
  ) { innerPadding ->
    LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      items(items = files, key = FileListItem::id) { file ->
        FileItem(file = file, onClick = { onOpen(file) }, onMore = { menuFile = file })
        HorizontalDivider()
      }
    }
  }

  renameFile?.let { file ->
    TextInputDialog(
      title = stringResource(ProfileRes.string.file_name),
      initialValue = file.name,
      hint = stringResource(ProfileRes.string.file_name),
      error = stringResource(ProfileRes.string.invalid_file_name),
      validator = ::isValidFileName,
      onDismiss = { renameFile = null },
      onConfirm = { newName ->
        onRename(file, newName)
        renameFile = null
      },
    )
  }
}

@Composable
private fun TextInputDialog(
  title: String,
  initialValue: String? = null,
  hint: String? = null,
  error: String? = null,
  validator: (String) -> Boolean = { true },
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit,
) {
  val initialText = initialValue.orEmpty()

  var inputText by remember {
    mutableStateOf(
      TextFieldValue(text = initialText, selection = TextRange(initialValue?.length ?: 0))
    )
  }
  var inputError by remember { mutableStateOf(if (!validator(initialText)) error else null) }
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
    keyboardController?.show()
  }

  val isValidInput = validator(inputText.text)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      OutlinedTextField(
        value = inputText,
        onValueChange = { newValue ->
          inputText = newValue
          inputError =
            if (!validator(newValue.text)) {
              error
            } else {
              null
            }
        },
        label = hint?.let { { Text(it) } },
        isError = inputError != null,
        supportingText = inputError?.let { { Text(it) } },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
      )
    },
    confirmButton = {
      TextButton(onClick = { onConfirm(inputText.text) }, enabled = isValidInput) {
        Text(stringResource(SharedRes.string.ok))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(SharedRes.string.cancel)) }
    },
  )
}

@Composable
private fun FileItem(file: FileListItem, onClick: () -> Unit, onMore: () -> Unit) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .heightIn(min = 56.dp)
        .clickable(onClick = onClick)
        .padding(end = 0.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier.size(width = 65.dp, height = 56.dp),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = if (file.isDirectory) TabbyIcons.OutlineFolder else TabbyIcons.OutlineArticle,
        contentDescription = null,
        modifier = Modifier.size(28.dp),
      )
    }

    Column(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
      Text(text = file.name)
      file.sizeText?.let {
        SizeSpacer(3.dp)
        Text(text = it, style = MaterialTheme.typography.bodyMedium)
      }
    }

    file.updatedAtText?.let {
      Text(
        text = it,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(horizontal = 8.dp),
      )
    }

    IconButton(onClick = onMore) {
      Icon(
        imageVector = TabbyIcons.BaselineMoreVert,
        contentDescription = stringResource(SharedRes.string.more),
      )
    }
  }
}

@Composable
private fun FilesMenuAction(
  icon: ImageVector,
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  tint: Color = MaterialTheme.colorScheme.onSurface,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 20.dp, vertical = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = tint,
      modifier = Modifier.size(24.dp),
    )
    SizeSpacer(16.dp)
    Text(text = text, color = tint)
  }
}

private fun isValidFileName(value: String): Boolean = tabbyIsValidFileNameInput(value)
