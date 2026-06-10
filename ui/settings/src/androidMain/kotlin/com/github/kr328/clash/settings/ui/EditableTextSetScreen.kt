package com.github.kr328.clash.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper

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
