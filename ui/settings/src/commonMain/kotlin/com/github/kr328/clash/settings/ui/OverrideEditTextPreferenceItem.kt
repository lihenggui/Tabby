package com.github.kr328.clash.settings.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import me.zhanghai.compose.preference.Preference

internal fun LazyListScope.overrideEditTextPreferenceItem(
  key: String,
  title: @Composable () -> String,
  placeholder: @Composable () -> String,
  emptyLabel: @Composable () -> String,
  value: String?,
  onValueChange: (String?) -> Unit,
  enabled: Boolean = true,
  numericOnly: Boolean = false,
  labels: @Composable () -> EditableTextEditorLabels = { editableTextEditorLabels() },
) {
  item(key = key, contentType = "EditTextPreference") {
    var showDialog by remember { mutableStateOf(false) }
    val titleText = title()
    val placeholderText = placeholder()
    val emptyLabelText = emptyLabel()
    val editorLabels = labels()
    val summary =
      when {
        value == null -> placeholderText
        value.isEmpty() -> emptyLabelText
        else -> value
      }
    Preference(
      title = { Text(titleText) },
      summary = { Text(summary) },
      enabled = enabled,
      onClick = { showDialog = true },
    )
    if (showDialog) {
      var inputText by remember(value) { mutableStateOf(initialTextFieldValue(value.orEmpty())) }
      val focusRequester = remember { FocusRequester() }
      val keyboardController = LocalSoftwareKeyboardController.current
      LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
      }
      AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text(titleText) },
        text = {
          OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = if (numericOnly) it.filterDigits() else it },
            keyboardOptions =
              if (numericOnly) {
                KeyboardOptions(keyboardType = KeyboardType.Number)
              } else {
                KeyboardOptions.Default
              },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
          )
        },
        confirmButton = {
          TextButton(
            onClick = {
              onValueChange(
                if (numericOnly) {
                  portText(parsePort(inputText.text))
                } else {
                  inputText.text
                }
              )
              showDialog = false
            }
          ) {
            Text(editorLabels.ok)
          }
        },
        dismissButton = {
          Row {
            TextButton(
              onClick = {
                onValueChange(null)
                showDialog = false
              }
            ) {
              Text(editorLabels.reset)
            }
            TextButton(onClick = { showDialog = false }) {
              Text(editorLabels.cancel)
            }
          }
        },
      )
    }
  }
}
