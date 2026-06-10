package com.github.kr328.clash.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper

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
