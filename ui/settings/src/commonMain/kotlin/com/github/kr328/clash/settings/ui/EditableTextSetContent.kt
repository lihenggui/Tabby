package com.github.kr328.clash.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineAdd
import com.github.kr328.clash.ui.icon.BaselineDragHandle
import com.github.kr328.clash.ui.icon.OutlineDelete
import com.github.kr328.clash.ui.icon.TabbyIcons
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
internal fun EditableTextSetContent(
  title: String,
  initialValues: Set<String>?,
  labels: EditableTextEditorLabels,
  onDismiss: () -> Unit,
  onApply: (Set<String>?) -> Unit,
) {
  val values = remember(initialValues) { initialValues.orEmpty().toMutableStateList() }
  var showAddDialog by remember { mutableStateOf(false) }
  var editingValue by remember { mutableStateOf<String?>(null) }
  val lazyListState = rememberLazyListState()
  val reorderableLazyListState =
    rememberReorderableLazyListState(lazyListState) { from, to ->
      values.apply { add(to.index, removeAt(from.index)) }
    }

  TabbyScaffold(
    title = title,
    onBack = onDismiss,
    actions = {
      IconButton(onClick = { showAddDialog = true }) {
        Icon(imageVector = TabbyIcons.BaselineAdd, contentDescription = labels.add)
      }
    },
  ) { innerPadding ->
    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      if (values.isEmpty()) {
        EmptyEditorContent(Modifier.weight(1f))
      } else {
        LazyColumn(state = lazyListState, modifier = Modifier.weight(1f)) {
          items(items = values, key = { it }) { value ->
            ReorderableItem(reorderableLazyListState, key = value) { _ ->
              ListItem(
                headlineContent = { Text(value) },
                modifier =
                  Modifier.clickable {
                    editingValue = value
                    showAddDialog = true
                  },
                leadingContent = {
                  Icon(
                    imageVector = TabbyIcons.BaselineDragHandle,
                    contentDescription = labels.reorder,
                    modifier = Modifier.draggableHandle(),
                  )
                },
                trailingContent = {
                  IconButton(onClick = { values.remove(value) }) {
                    Icon(
                      imageVector = TabbyIcons.OutlineDelete,
                      contentDescription = labels.delete,
                    )
                  }
                },
              )
              HorizontalDivider()
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End,
      ) {
        TextButton(onClick = { onApply(null) }) { Text(labels.reset) }
        TextButton(onClick = onDismiss) { Text(labels.cancel) }
        TextButton(onClick = { onApply(values.toSet()) }) { Text(labels.ok) }
      }
    }
  }

  if (showAddDialog) {
    SingleTextInputDialog(
      title = title,
      initialText = editingValue.orEmpty(),
      labels = labels,
      onDismiss = {
        showAddDialog = false
        editingValue = null
      },
      onConfirm = { newValue ->
        val normalizedValue = newValue.trim()

        if (normalizedValue.isNotBlank()) {
          if (editingValue == null) {
            if (!values.contains(normalizedValue)) {
              values.add(normalizedValue)
            }
          } else if (!values.contains(normalizedValue) || editingValue == normalizedValue) {
            val idx = values.indexOf(editingValue)
            if (idx >= 0) values[idx] = normalizedValue
          }
        }

        showAddDialog = false
        editingValue = null
      },
    )
  }
}

@Composable
private fun SingleTextInputDialog(
  title: String,
  initialText: String,
  labels: EditableTextEditorLabels,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit,
) {
  var inputText by remember(initialText) { mutableStateOf(initialTextFieldValue(initialText)) }
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
    keyboardController?.show()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      OutlinedTextField(
        value = inputText,
        onValueChange = { inputText = it },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
      )
    },
    confirmButton = { TextButton(onClick = { onConfirm(inputText.text) }) { Text(labels.ok) } },
    dismissButton = { TextButton(onClick = onDismiss) { Text(labels.cancel) } },
  )
}
