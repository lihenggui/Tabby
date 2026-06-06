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
internal data class EditableTextList(
  val title: EditableTextTitle,
  val initialValues: Set<String>?,
) : NavKey

internal fun EntryProviderScope<NavKey>.editableTextSetScreenEntry(
  onDismiss: () -> Unit,
  onApply: (Set<String>?) -> Unit,
) {
  entry<EditableTextList> { key ->
    EditableTextSetScreen(
      title = key.title,
      initialValues = key.initialValues,
      onDismiss = onDismiss,
      onApply = onApply,
    )
  }
}

@Composable
private fun EditableTextSetScreen(
  title: EditableTextTitle,
  initialValues: Set<String>?,
  onDismiss: () -> Unit,
  onApply: (Set<String>?) -> Unit,
) {
  EditableTextSetContent(
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
private fun EditableTextSetScreenPreview() {
  EditableTextSetScreen(
    title = EditableTextTitle.SniffHttpPorts,
    initialValues = setOf("80", "8080"),
    onDismiss = {},
    onApply = {},
  )
}
