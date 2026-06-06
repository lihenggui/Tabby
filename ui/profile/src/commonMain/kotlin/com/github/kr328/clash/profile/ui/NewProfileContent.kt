package com.github.kr328.clash.profile.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import com.github.kr328.clash.ui.component.SizeSpacer
import com.github.kr328.clash.ui.component.Spacer
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.theme.tabbyDimens
import org.jetbrains.compose.resources.stringResource
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.new_profile

internal data class NewProfileProviderItem(
  val name: String,
  val summary: String,
  val iconPainter: Painter?,
  val hasDetail: Boolean,
)

@Composable
internal fun NewProfileContent(
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState,
  providers: List<NewProfileProviderItem>,
  onCreate: (Int) -> Unit,
  onDetail: (Int) -> Unit,
) {
  TabbyScaffold(
    title = stringResource(SharedRes.string.new_profile),
    modifier = modifier,
    snackbarHostState = snackbarHostState,
  ) { innerPadding ->
    LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      itemsIndexed(items = providers, key = { index, provider -> "${provider.name}-$index" }) {
        index,
        provider ->
        ProfileProviderItem(
          provider = provider,
          onClick = { onCreate(index) },
          onLongClick = { if (provider.hasDetail) onDetail(index) },
        )
      }
    }
  }
}

@Composable
private fun ProfileProviderItem(
  provider: NewProfileProviderItem,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
) {
  val dimens = tabbyDimens
  val itemPaddingVertical = dimens.itemPaddingVertical
  val headerSize = dimens.itemHeaderComponentSize
  val headerMargin = dimens.itemHeaderMargin
  val textMargin = dimens.itemTextMargin

  Row(
    modifier =
      Modifier.fillMaxWidth()
        .combinedClickable(onClick = onClick, onLongClick = onLongClick)
        .padding(vertical = itemPaddingVertical),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(headerMargin)
    val iconPainter = provider.iconPainter
    if (iconPainter != null) {
      Icon(painter = iconPainter, contentDescription = null, modifier = Modifier.size(headerSize))
    } else {
      SizeSpacer(headerSize)
    }
    Spacer(headerMargin)
    Column {
      Text(text = provider.name, style = MaterialTheme.typography.bodyLarge)
      Text(
        text = provider.summary,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = textMargin),
      )
    }
  }
}
