package com.github.kr328.clash.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.ui.component.Spacer
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineAdd
import com.github.kr328.clash.ui.icon.BaselineContentCopy
import com.github.kr328.clash.ui.icon.BaselineEdit
import com.github.kr328.clash.ui.icon.BaselineMoreVert
import com.github.kr328.clash.ui.icon.BaselineSync
import com.github.kr328.clash.ui.icon.BaselineUpdate
import com.github.kr328.clash.ui.icon.OutlineDelete
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.theme.tabbyDimens
import org.jetbrains.compose.resources.stringResource
import tabby.ui.profile.generated.resources.Res as ProfileRes
import tabby.ui.profile.generated.resources.duplicate
import tabby.ui.profile.generated.resources.edit
import tabby.ui.profile.generated.resources.update
import tabby.ui.profile.generated.resources.update_all
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.delete
import tabby.ui.shared.generated.resources.more
import tabby.ui.shared.generated.resources.new_profile
import tabby.ui.shared.generated.resources.profiles

internal data class ProfileListItem(
  val profile: Profile,
  val typeText: String,
  val usageText: String?,
  val expireText: String?,
  val updatedAtText: String,
  val trafficProgress: Int,
)

@Composable
internal fun ProfilesContent(
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState,
  profiles: List<ProfileListItem>,
  allUpdating: Boolean,
  hasUpdatableProfile: Boolean,
  onUpdateAll: () -> Unit,
  onCreate: () -> Unit,
  onActivate: (Profile) -> Unit,
  onUpdate: (Profile) -> Unit,
  onEdit: (Profile) -> Unit,
  onDuplicate: (Profile) -> Unit,
  onDelete: (Profile) -> Unit,
) {
  var menuItem by remember { mutableStateOf<ProfileListItem?>(null) }

  menuItem?.let { item ->
    val profile = item.profile
    ModalBottomSheet(
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
      onDismissRequest = { menuItem = null },
    ) {
      if (profile.imported && profile.type != Profile.Type.File) {
        ProfilesMenuAction(
          icon = TabbyIcons.BaselineUpdate,
          text = stringResource(ProfileRes.string.update),
          onClick = {
            menuItem = null
            onUpdate(profile)
          },
        )
      }
      ProfilesMenuAction(
        icon = TabbyIcons.BaselineEdit,
        text = stringResource(ProfileRes.string.edit),
        onClick = {
          menuItem = null
          onEdit(profile)
        },
      )
      if (profile.imported) {
        ProfilesMenuAction(
          icon = TabbyIcons.BaselineContentCopy,
          text = stringResource(ProfileRes.string.duplicate),
          onClick = {
            menuItem = null
            onDuplicate(profile)
          },
        )
      }
      ProfilesMenuAction(
        icon = TabbyIcons.OutlineDelete,
        text = stringResource(SharedRes.string.delete),
        tint = MaterialTheme.colorScheme.error,
        onClick = {
          menuItem = null
          onDelete(profile)
        },
      )
      Spacer(16.dp)
    }
  }

  TabbyScaffold(
    title = stringResource(SharedRes.string.profiles),
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    actions = {
      if (hasUpdatableProfile) {
        IconButton(onClick = onUpdateAll, enabled = !allUpdating) {
          if (allUpdating) {
            CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.5.dp)
          } else {
            Icon(
              imageVector = TabbyIcons.BaselineSync,
              contentDescription = stringResource(ProfileRes.string.update_all),
            )
          }
        }
      }
      IconButton(onClick = onCreate) {
        Icon(
          imageVector = TabbyIcons.BaselineAdd,
          contentDescription = stringResource(SharedRes.string.new_profile),
        )
      }
    },
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentPadding = PaddingValues(vertical = 5.dp),
      verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
      items(items = profiles, key = { it.profile.uuid }) { item ->
        ProfileItem(
          item = item,
          onClick = { onActivate(item.profile) },
          onMenuClick = { menuItem = item },
        )
      }
    }
  }
}

@Composable
private fun ProfileItem(item: ProfileListItem, onClick: () -> Unit, onMenuClick: () -> Unit) {
  val profile = item.profile
  val dimens = tabbyDimens
  val itemMinHeight = dimens.itemMinHeight
  val itemHeaderMargin = dimens.itemHeaderMargin
  val itemTextMargin = dimens.itemTextMargin
  val showTraffic = item.usageText != null

  ElevatedCard(
    modifier = Modifier.fillMaxWidth().padding(horizontal = itemHeaderMargin, vertical = 5.dp),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(start = 0.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier.size(width = 65.dp, height = itemMinHeight),
        contentAlignment = Alignment.Center,
      ) {
        RadioButton(selected = profile.active, onClick = null)
      }

      Column(modifier = Modifier.weight(1f).padding(vertical = dimens.itemPaddingVertical)) {
        Text(text = profile.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(itemTextMargin)
        Text(text = item.typeText, style = MaterialTheme.typography.bodyMedium)
        item.usageText?.let {
          Spacer(4.dp)
          Text(text = it, style = MaterialTheme.typography.labelMedium)
        }
        item.expireText?.let {
          Spacer(4.dp)
          Text(text = it, style = MaterialTheme.typography.labelMedium)
        }
        if (showTraffic) {
          Spacer(6.dp)
          LinearProgressIndicator(
            progress = { item.trafficProgress / 1000f },
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }

      Text(
        text = item.updatedAtText,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(horizontal = 10.dp),
      )

      Box(
        modifier =
          Modifier.width(1.dp).height(itemMinHeight).background(MaterialTheme.colorScheme.outline)
      )

      IconButton(onClick = onMenuClick, modifier = Modifier.padding(horizontal = 4.dp)) {
        Icon(
          imageVector = TabbyIcons.BaselineMoreVert,
          contentDescription = stringResource(SharedRes.string.more),
        )
      }
    }
  }
}

@Composable
private fun ProfilesMenuAction(
  icon: ImageVector,
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  tint: Color = MaterialTheme.colorScheme.onSurface,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 20.dp, vertical = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = tint,
      modifier = Modifier.size(24.dp),
    )
    Spacer(16.dp)
    Text(text = text, color = tint)
  }
}
