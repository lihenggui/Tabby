package com.github.kr328.clash.settings.ui

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource

@Serializable
internal data class EditableTextMap(
  val title: EditableTextTitle,
  val initialValues: Map<String, String>?,
) : NavKey

internal fun EntryProviderScope<NavKey>.editableTextMapScreenEntry(
  onDismiss: () -> Unit,
  onApply: (Map<String, String>?) -> Unit,
) {
  entry<EditableTextMap> { key ->
    EditableTextMapScreen(
      title = key.title,
      initialValues = key.initialValues,
      onDismiss = onDismiss,
      onApply = onApply,
    )
  }
}

@Composable
internal fun EditableTextMapScreen(
  title: EditableTextTitle,
  initialValues: Map<String, String>?,
  onDismiss: () -> Unit,
  onApply: (Map<String, String>?) -> Unit,
) {
  EditableTextMapContent(
    title = stringResource(title.textResource),
    initialValues = initialValues,
    labels = editableTextEditorLabels(),
    onDismiss = onDismiss,
    onApply = onApply,
  )
}
