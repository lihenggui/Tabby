package com.github.kr328.clash.log.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineDelete
import com.github.kr328.clash.ui.icon.BaselineSave
import com.github.kr328.clash.ui.icon.BaselineStop
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.theme.tabbyDimens
import org.jetbrains.compose.resources.stringResource
import tabby.ui.log.generated.resources.Res as LogRes
import tabby.ui.log.generated.resources.tabby_logcat
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.close
import tabby.ui.shared.generated.resources.delete
import tabby.ui.shared.generated.resources.export

@Composable
internal fun LogcatContent(
  streaming: Boolean,
  messages: List<LogMessage>,
  listState: LazyListState,
  snackbarHostState: SnackbarHostState,
  formatMessageTime: (Long) -> String,
  onClose: () -> Unit,
  onDelete: () -> Unit,
  onExport: () -> Unit,
  onCopyMessage: (LogMessage) -> Unit,
  modifier: Modifier = Modifier,
) {
  TabbyScaffold(
    title = stringResource(LogRes.string.tabby_logcat),
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    actions = {
      if (streaming) {
        IconButton(onClick = onClose) {
          Icon(
            imageVector = TabbyIcons.BaselineStop,
            contentDescription = stringResource(SharedRes.string.close),
          )
        }
      } else {
        IconButton(onClick = onDelete) {
          Icon(
            imageVector = TabbyIcons.BaselineDelete,
            contentDescription = stringResource(SharedRes.string.delete),
          )
        }
        IconButton(onClick = onExport) {
          Icon(
            imageVector = TabbyIcons.BaselineSave,
            contentDescription = stringResource(SharedRes.string.export),
          )
        }
      }
    },
  ) { innerPadding ->
    LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding), state = listState) {
      items(items = messages) { message ->
        LogcatMessageItem(
          message = message,
          timeText = formatMessageTime(message.time),
          onCopyMessage = onCopyMessage,
        )
      }
    }
  }
}

@Composable
private fun LogcatMessageItem(
  message: LogMessage,
  timeText: String,
  onCopyMessage: (LogMessage) -> Unit,
) {
  val dimens = tabbyDimens
  Column(
    modifier =
      Modifier.fillMaxWidth()
        .combinedClickable(onClick = {}, onLongClick = { onCopyMessage(message) })
        .padding(12.dp),
    verticalArrangement = Arrangement.Center,
  ) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = message.level.name,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
      )
      Text(
        text = timeText,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.weight(1f),
        textAlign = TextAlign.End,
      )
    }
    Text(
      text = message.message,
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.fillMaxWidth().padding(top = dimens.itemTextMargin),
    )
  }
}
