package com.github.kr328.clash.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.ui.component.Spacer
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineSwapVert
import com.github.kr328.clash.ui.icon.BaselineSync
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.theme.tabbyDimens
import org.jetbrains.compose.resources.stringResource
import tabby.ui.profile.generated.resources.Res as ProfileRes
import tabby.ui.profile.generated.resources.update
import tabby.ui.profile.generated.resources.update_all
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.providers

internal data class ProviderListItem(
  val provider: Provider,
  val typeText: String,
  val updatedAtText: String,
  val updating: Boolean,
)

@Composable
internal fun ProvidersContent(
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState,
  providers: List<ProviderListItem>,
  onUpdateAll: () -> Unit,
  onUpdate: (Int, Provider) -> Unit,
) {
  TabbyScaffold(
    title = stringResource(SharedRes.string.providers),
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    actions = {
      IconButton(onClick = onUpdateAll) {
        Icon(
          imageVector = TabbyIcons.BaselineSync,
          contentDescription = stringResource(ProfileRes.string.update_all),
        )
      }
    },
  ) { innerPadding ->
    LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      itemsIndexed(
        items = providers,
        key = { _, state -> "${state.provider.type}-${state.provider.name}" },
      ) { index, state ->
        ProviderItem(state = state, onUpdate = { onUpdate(index, state.provider) })
      }
    }
  }
}

@Composable
private fun ProviderItem(state: ProviderListItem, onUpdate: () -> Unit) {
  val dimens = tabbyDimens
  val itemMinHeight = dimens.itemMinHeight
  val itemHeaderMargin = dimens.itemHeaderMargin
  val itemTextMargin = dimens.itemTextMargin

  val canUpdate = state.provider.vehicleType != Provider.VehicleType.Inline

  Row(
    modifier =
      Modifier.fillMaxWidth().heightIn(min = itemMinHeight).padding(start = itemHeaderMargin),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = state.provider.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
      Spacer(itemTextMargin)
      Text(text = state.typeText, style = MaterialTheme.typography.bodyMedium)
    }

    if (canUpdate) {
      Text(text = state.updatedAtText, modifier = Modifier.padding(end = 10.dp))
      Box(
        modifier =
          Modifier.size(width = 1.dp, height = itemMinHeight)
            .background(MaterialTheme.colorScheme.outline)
      )
      IconButton(
        onClick = onUpdate,
        enabled = !state.updating,
        modifier = Modifier.padding(horizontal = 4.dp),
      ) {
        if (state.updating) {
          CircularProgressIndicator(modifier = Modifier.size(30.dp), strokeWidth = 2.dp)
        } else {
          Icon(
            imageVector = TabbyIcons.BaselineSwapVert,
            contentDescription = stringResource(ProfileRes.string.update),
          )
        }
      }
    }
  }
}
