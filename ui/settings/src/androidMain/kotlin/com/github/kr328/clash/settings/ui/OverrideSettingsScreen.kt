package com.github.kr328.clash.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.settings.vm.OverrideSettingsViewModel
import com.github.kr328.clash.ui.lifecycle.viewModelWithLifecycle
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper

@Composable
internal fun OverrideSettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: OverrideSettingsViewModel = viewModelWithLifecycle(),
  onResetCompleted: () -> Unit,
) {
  OverrideSettingsRouteContent { onOpenEditableTextMap, onOpenEditableTextList ->
    val configuration by viewModel.configuration.collectAsStateWithLifecycle()
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    OverrideSettingsContent(
      configuration = configuration,
      actions = viewModel,
      modifier = modifier,
      showResetConfirmDialog = showResetConfirmDialog,
      onShowResetConfirmDialogChange = { showResetConfirmDialog = it },
      onResetConfirmed = {
        viewModel.resetOverride()
        onResetCompleted()
      },
      onOpenEditableTextMap = onOpenEditableTextMap,
      onOpenEditableTextList = onOpenEditableTextList,
    )
  }
}

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun OverrideSettingsContentPreview() {
  OverrideSettingsContent(
    configuration = ConfigurationOverride(),
    actions = object : OverrideSettingsActions {},
    showResetConfirmDialog = false,
    onShowResetConfirmDialogChange = {},
    onResetConfirmed = {},
    onOpenEditableTextMap = { _, _, _ -> },
    onOpenEditableTextList = { _, _, _ -> },
  )
}
