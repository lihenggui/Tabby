package com.github.kr328.clash.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper
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
private fun EditableTextMapScreen(
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

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun EditableTextMapScreenPreview() {
  EditableTextMapScreen(
    title = EditableTextTitle.Hosts,
    initialValues = mapOf("example.com" to "127.0.0.1", "test.com" to "192.168.1.1"),
    onDismiss = {},
    onApply = {},
  )
}
