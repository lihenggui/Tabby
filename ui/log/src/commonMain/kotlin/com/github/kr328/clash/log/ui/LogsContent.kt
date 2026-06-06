package com.github.kr328.clash.log.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.kr328.clash.ui.component.SizeSpacer
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineAdb
import com.github.kr328.clash.ui.icon.BaselineClearAll
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.theme.tabbyDimens
import org.jetbrains.compose.resources.stringResource
import tabby.ui.log.generated.resources.Res as LogRes
import tabby.ui.log.generated.resources.delete_all_logs
import tabby.ui.log.generated.resources.delete_all_logs_warn
import tabby.ui.log.generated.resources.history
import tabby.ui.log.generated.resources.tabby_logcat
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.cancel
import tabby.ui.shared.generated.resources.logs
import tabby.ui.shared.generated.resources.ok
import tabby.ui.shared.generated.resources.tap_to_start

internal data class LogListItem(val fileName: String, val createdText: String)

@Composable
internal fun LogsContent(
  logs: List<LogListItem>,
  showDeleteAllAction: Boolean = true,
  onDeleteAll: () -> Unit,
  onStartLogcat: () -> Unit,
  onOpenFile: (LogListItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showDeleteAllDialog by remember { mutableStateOf(false) }

  if (showDeleteAllDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteAllDialog = false },
      title = { Text(text = stringResource(LogRes.string.delete_all_logs)) },
      text = { Text(text = stringResource(LogRes.string.delete_all_logs_warn)) },
      confirmButton = {
        TextButton(
          onClick = {
            showDeleteAllDialog = false
            onDeleteAll()
          }
        ) {
          Text(text = stringResource(SharedRes.string.ok))
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteAllDialog = false }) {
          Text(text = stringResource(SharedRes.string.cancel))
        }
      },
    )
  }

  TabbyScaffold(
    modifier = modifier,
    title = stringResource(SharedRes.string.logs),
    actions = {
      if (showDeleteAllAction) {
        IconButton(onClick = { showDeleteAllDialog = true }) {
          Icon(
            imageVector = TabbyIcons.BaselineClearAll,
            contentDescription = stringResource(LogRes.string.delete_all_logs),
          )
        }
      }
    },
  ) { innerPadding ->
    val dimens = tabbyDimens

    LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      item {
        LogsActionItem(
          title = stringResource(LogRes.string.tabby_logcat),
          summary = stringResource(SharedRes.string.tap_to_start),
          icon = TabbyIcons.BaselineAdb,
          onClick = onStartLogcat,
        )
      }
      item { HorizontalDivider() }
      item {
        Text(
          text = stringResource(LogRes.string.history),
          color = MaterialTheme.colorScheme.primary,
          modifier =
            Modifier.fillMaxWidth()
              .padding(
                start = dimens.itemHeaderComponentSize + dimens.itemHeaderMargin * 2,
                end = dimens.settingsItemEndPadding,
                top = dimens.itemPaddingVertical,
                bottom = dimens.itemPaddingVertical,
              ),
        )
      }
      items(items = logs, key = { it.fileName }) { file ->
        LogsActionItem(
          title = file.fileName,
          summary = file.createdText,
          onClick = { onOpenFile(file) },
        )
      }
    }
  }
}

@Composable
private fun LogsActionItem(
  title: String,
  summary: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
) {
  val dimens = tabbyDimens
  val headerLayoutWidth = dimens.itemHeaderComponentSize + dimens.itemHeaderMargin * 2
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(end = dimens.settingsItemEndPadding),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier.size(width = headerLayoutWidth, height = dimens.itemMinHeight),
      contentAlignment = Alignment.Center,
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          modifier = Modifier.size(dimens.itemHeaderComponentSize),
        )
      }
    }

    Column(
      modifier = Modifier.heightIn(min = dimens.itemMinHeight),
      verticalArrangement = Arrangement.Center,
    ) {
      Text(text = title)
      SizeSpacer(dimens.itemTextMargin)
      Text(text = summary, style = MaterialTheme.typography.bodyMedium)
    }
  }
}
