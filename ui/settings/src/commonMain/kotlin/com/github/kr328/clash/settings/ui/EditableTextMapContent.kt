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
internal fun EditableTextMapContent(
  title: String,
  initialValues: Map<String, String>?,
  labels: EditableTextEditorLabels,
  onDismiss: () -> Unit,
  onApply: (Map<String, String>?) -> Unit,
) {
  val values =
    remember(initialValues) {
      initialValues?.entries.orEmpty().map { it.toPair() }.toMutableStateList()
    }
  var showAddDialog by remember { mutableStateOf(false) }
  var editingKey by remember { mutableStateOf<String?>(null) }
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
          items(items = values, key = { it.first }) { entry ->
            val (key, value) = entry
            ReorderableItem(reorderableLazyListState, key = key) { _ ->
              ListItem(
                headlineContent = { Text(key) },
                supportingContent = { Text(value) },
                modifier =
                  Modifier.clickable {
                    editingKey = key
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
                  IconButton(onClick = { values.remove(entry) }) {
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
        TextButton(onClick = { onApply(values.toMap()) }) { Text(labels.ok) }
      }
    }
  }

  if (showAddDialog) {
    val editingEntry = editingKey?.let { k -> values.firstOrNull { it.first == k } }
    MapEntryInputDialog(
      title = title,
      initialKey = editingEntry?.first.orEmpty(),
      initialValue = editingEntry?.second.orEmpty(),
      labels = labels,
      onDismiss = {
        showAddDialog = false
        editingKey = null
      },
      onConfirm = { key, valueText ->
        if (editingKey == null) {
          val index = values.indexOfFirst { it.first == key }
          if (index >= 0) values[index] = key to valueText else values.add(key to valueText)
        } else {
          val currentIdx = values.indexOfFirst { it.first == editingKey }
          val existingIndex = values.indexOfFirst { it.first == key }
          when {
            existingIndex < 0 || existingIndex == currentIdx -> {
              if (currentIdx >= 0) values[currentIdx] = key to valueText
            }
            else -> {
              values[existingIndex] = key to valueText
              if (currentIdx >= 0) values.removeAt(currentIdx)
            }
          }
        }

        showAddDialog = false
        editingKey = null
      },
    )
  }
}

@Composable
private fun MapEntryInputDialog(
  title: String,
  initialKey: String,
  initialValue: String,
  labels: EditableTextEditorLabels,
  onDismiss: () -> Unit,
  onConfirm: (String, String) -> Unit,
) {
  var keyText by remember(initialKey) { mutableStateOf(initialTextFieldValue(initialKey)) }
  var valueText by remember(initialValue) { mutableStateOf(initialTextFieldValue(initialValue)) }
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  val confirmEnabled = keyText.text.isNotBlank() && valueText.text.isNotBlank()

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
    keyboardController?.show()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = keyText,
          onValueChange = { keyText = it },
          label = { Text(labels.key) },
          placeholder = { Text(labels.key) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        )
        OutlinedTextField(
          value = valueText,
          onValueChange = { valueText = it },
          label = { Text(labels.value) },
          placeholder = { Text(labels.value) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = { onConfirm(keyText.text.trim(), valueText.text.trim()) },
        enabled = confirmEnabled,
      ) {
        Text(labels.ok)
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(labels.cancel) } },
  )
}
