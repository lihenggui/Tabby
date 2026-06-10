package com.github.kr328.clash.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.core.model.AccessControlSort
import com.github.kr328.clash.ui.component.Spacer
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineMoreVert
import com.github.kr328.clash.ui.icon.BaselineSearch
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.theme.tabbyDimens
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import tabby.ui.settings.generated.resources.Res
import tabby.ui.settings.generated.resources.access_control_packages
import tabby.ui.settings.generated.resources.export_to_clipboard
import tabby.ui.settings.generated.resources.external
import tabby.ui.settings.generated.resources.filter
import tabby.ui.settings.generated.resources.import_from_clipboard
import tabby.ui.settings.generated.resources.install_time
import tabby.ui.settings.generated.resources.keyword
import tabby.ui.settings.generated.resources.more
import tabby.ui.settings.generated.resources.name
import tabby.ui.settings.generated.resources.package_name
import tabby.ui.settings.generated.resources.reverse
import tabby.ui.settings.generated.resources.search
import tabby.ui.settings.generated.resources.select_all
import tabby.ui.settings.generated.resources.select_invert
import tabby.ui.settings.generated.resources.select_none
import tabby.ui.settings.generated.resources.sort
import tabby.ui.settings.generated.resources.system_apps
import tabby.ui.settings.generated.resources.update_time

@Composable
internal fun <T> AccessControlContent(
  apps: List<T>,
  selected: Set<String>,
  sort: AccessControlSort,
  reverse: Boolean,
  showSystemApps: Boolean,
  actions: AccessControlActions,
  appPackageName: (T) -> String,
  appLabel: (T) -> String,
  modifier: Modifier = Modifier,
  appIcon: @Composable (T) -> Unit = {},
) {
  var showSearch by remember { mutableStateOf(false) }
  var showMenu by remember { mutableStateOf(false) }

  if (showMenu) {
    ModalBottomSheet(
      onDismissRequest = { showMenu = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
      AccessControlMenuContent(
        sort = sort,
        reverse = reverse,
        showSystemApps = showSystemApps,
        onSelectAll = {
          showMenu = false
          actions.selectAll()
        },
        onSelectNone = {
          showMenu = false
          actions.selectNone()
        },
        onSelectInvert = {
          showMenu = false
          actions.selectInvert()
        },
        onImport = {
          showMenu = false
          actions.importFromClipboard()
        },
        onExport = {
          showMenu = false
          actions.exportToClipboard()
        },
        onUpdateSort = {
          showMenu = false
          actions.updateSort(it)
        },
        onUpdateReverse = {
          showMenu = false
          actions.updateReverse(it)
        },
        onUpdateShowSystemApps = {
          showMenu = false
          actions.updateShowSystemApps(it)
        },
      )
      Spacer(16.dp)
    }
  }

  if (showSearch) {
    ModalBottomSheet(
      onDismissRequest = { showSearch = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
      AccessControlSearchContent(
        apps = apps,
        selected = selected,
        appPackageName = appPackageName,
        appLabel = appLabel,
        appIcon = appIcon,
        onToggleApp = actions::toggleApp,
      )
    }
  }

  TabbyScaffold(
    title = stringResource(Res.string.access_control_packages),
    modifier = modifier,
    actions = {
      IconButton(onClick = { showSearch = true }) {
        Icon(
          imageVector = TabbyIcons.BaselineSearch,
          contentDescription = stringResource(Res.string.search),
        )
      }
      IconButton(onClick = { showMenu = true }) {
        Icon(
          imageVector = TabbyIcons.BaselineMoreVert,
          contentDescription = stringResource(Res.string.more),
        )
      }
    },
  ) { innerPadding ->
    LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      items(items = apps, key = appPackageName) { app ->
        AccessControlAppItem(
          label = appLabel(app),
          packageName = appPackageName(app),
          selected = appPackageName(app) in selected,
          icon = { appIcon(app) },
          onClick = { actions.toggleApp(appPackageName(app)) },
        )
        HorizontalDivider()
      }
    }
  }
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
private fun <T> ColumnScope.AccessControlSearchContent(
  apps: List<T>,
  selected: Set<String>,
  appPackageName: (T) -> String,
  appLabel: (T) -> String,
  appIcon: @Composable (T) -> Unit,
  onToggleApp: (String) -> Unit,
) {
  var keyword by rememberSaveable { mutableStateOf("") }
  var filtered by remember(apps) { mutableStateOf(emptyList<T>()) }

  LaunchedEffect(apps) {
    snapshotFlow { keyword }
      .debounce(200.milliseconds)
      .distinctUntilChanged()
      .mapLatest { currentKeyword ->
        if (currentKeyword.isBlank()) {
          emptyList()
        } else {
          withContext(Dispatchers.Default) {
            filterAccessControlApps(
              apps = apps,
              keyword = currentKeyword,
              label = appLabel,
              packageName = appPackageName,
            )
          }
        }
      }
      .collect { filtered = it }
  }

  TextField(
    value = keyword,
    onValueChange = { keyword = it },
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    placeholder = { Text(text = stringResource(Res.string.keyword)) },
    singleLine = true,
    colors = TextFieldDefaults.colors(),
  )

  Spacer(8.dp)

  LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp)) {
    items(items = filtered, key = appPackageName) { app ->
      AccessControlAppItem(
        label = appLabel(app),
        packageName = appPackageName(app),
        selected = appPackageName(app) in selected,
        icon = { appIcon(app) },
        onClick = { onToggleApp(appPackageName(app)) },
      )
      HorizontalDivider()
    }
  }

  Spacer(16.dp)
}

@Composable
private fun ColumnScope.AccessControlMenuContent(
  sort: AccessControlSort,
  reverse: Boolean,
  showSystemApps: Boolean,
  onSelectAll: () -> Unit,
  onSelectNone: () -> Unit,
  onSelectInvert: () -> Unit,
  onImport: () -> Unit,
  onExport: () -> Unit,
  onUpdateSort: (AccessControlSort) -> Unit,
  onUpdateReverse: (Boolean) -> Unit,
  onUpdateShowSystemApps: (Boolean) -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    AccessControlMenuAction(text = stringResource(Res.string.select_all), onClick = onSelectAll)
    AccessControlMenuAction(text = stringResource(Res.string.select_none), onClick = onSelectNone)
    AccessControlMenuAction(
      text = stringResource(Res.string.select_invert),
      onClick = onSelectInvert,
    )

    AccessControlMenuSectionTitle(text = stringResource(Res.string.filter))
    AccessControlMenuCheckAction(
      text = stringResource(Res.string.system_apps),
      checked = showSystemApps,
      onCheckedChange = onUpdateShowSystemApps,
    )

    AccessControlMenuSectionTitle(text = stringResource(Res.string.sort))
    AccessControlMenuSortAction(
      text = stringResource(Res.string.name),
      checked = sort == AccessControlSort.Label,
      onClick = { onUpdateSort(AccessControlSort.Label) },
    )
    AccessControlMenuSortAction(
      text = stringResource(Res.string.package_name),
      checked = sort == AccessControlSort.PackageName,
      onClick = { onUpdateSort(AccessControlSort.PackageName) },
    )
    AccessControlMenuSortAction(
      text = stringResource(Res.string.install_time),
      checked = sort == AccessControlSort.InstallTime,
      onClick = { onUpdateSort(AccessControlSort.InstallTime) },
    )
    AccessControlMenuSortAction(
      text = stringResource(Res.string.update_time),
      checked = sort == AccessControlSort.UpdateTime,
      onClick = { onUpdateSort(AccessControlSort.UpdateTime) },
    )
    AccessControlMenuCheckAction(
      text = stringResource(Res.string.reverse),
      checked = reverse,
      onCheckedChange = onUpdateReverse,
    )

    AccessControlMenuSectionTitle(text = stringResource(Res.string.external))
    AccessControlMenuAction(
      text = stringResource(Res.string.import_from_clipboard),
      onClick = onImport,
    )
    AccessControlMenuAction(
      text = stringResource(Res.string.export_to_clipboard),
      onClick = onExport,
    )
  }
}

@Composable
private fun AccessControlMenuSectionTitle(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.titleSmall,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
  )
}

@Composable
private fun AccessControlMenuAction(text: String, onClick: () -> Unit) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 20.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(text = text)
  }
}

@Composable
private fun AccessControlMenuSortAction(text: String, checked: Boolean, onClick: () -> Unit) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .selectable(selected = checked, onClick = onClick, role = Role.RadioButton)
        .padding(horizontal = 8.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    RadioButton(selected = checked, onClick = null)
    Text(text = text, modifier = Modifier.padding(start = 8.dp))
  }
}

@Composable
private fun AccessControlMenuCheckAction(
  text: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .toggleable(value = checked, onValueChange = onCheckedChange, role = Role.Checkbox)
        .padding(horizontal = 8.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Checkbox(checked = checked, onCheckedChange = null)
    Text(text = text, modifier = Modifier.padding(start = 8.dp))
  }
}

@Composable
private fun AccessControlAppItem(
  label: String,
  packageName: String,
  selected: Boolean,
  icon: @Composable () -> Unit,
  onClick: () -> Unit,
) {
  val dimens = tabbyDimens
  val itemMinHeight = dimens.itemMinHeight
  val itemTextMargin = dimens.itemTextMargin

  Row(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(end = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier.size(width = 65.dp, height = itemMinHeight),
      contentAlignment = Alignment.Center,
    ) {
      icon()
    }

    Column(
      modifier = Modifier.weight(1f).padding(vertical = dimens.itemPaddingVertical),
      verticalArrangement = Arrangement.Center,
    ) {
      Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis)
      Spacer(itemTextMargin)
      Text(
        text = packageName,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }

    Checkbox(checked = selected, onCheckedChange = { onClick() })
  }
}

internal interface AccessControlActions {
  fun toggleApp(packageName: String) = Unit

  fun selectAll() = Unit

  fun selectNone() = Unit

  fun selectInvert() = Unit

  fun importFromClipboard() = Unit

  fun exportToClipboard() = Unit

  fun updateSort(sort: AccessControlSort) = Unit

  fun updateReverse(reverse: Boolean) = Unit

  fun updateShowSystemApps(show: Boolean) = Unit
}
