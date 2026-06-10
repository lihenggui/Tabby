package com.github.kr328.clash.crash.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.ui.component.TabbyScaffold
import org.jetbrains.compose.resources.stringResource
import tabby.ui.crash.generated.resources.Res as CrashRes
import tabby.ui.crash.generated.resources.application_crashed

@Composable
internal fun AppCrashedContent(modifier: Modifier = Modifier, logs: String) {
  TabbyScaffold(modifier = modifier, title = stringResource(CrashRes.string.application_crashed)) {
    innerPadding ->
    SelectionContainer {
      Text(
        text = logs,
        style =
          MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5,
          ),
        modifier =
          Modifier.fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
      )
    }
  }
}
