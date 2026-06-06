package com.github.kr328.clash.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.settings.vm.OverrideSettingsViewModel
import com.github.kr328.clash.ui.lifecycle.viewModelWithLifecycle
import com.github.kr328.clash.ui.nav.TabbyNavDisplay
import com.github.kr328.clash.ui.nav.addIfNotLast
import com.github.kr328.clash.ui.nav.rememberNavBackStackBuilder
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper
import kotlinx.serialization.Serializable

private sealed interface OverrideSettingsRoute : NavKey {
  @Serializable data object Main : OverrideSettingsRoute
}

@Composable
internal fun OverrideSettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: OverrideSettingsViewModel = viewModelWithLifecycle(),
  onResetCompleted: () -> Unit,
) {
  val backStack = rememberNavBackStackBuilder { add(OverrideSettingsRoute.Main) }
  var currentEditableTextMapOnApply by remember {
    mutableStateOf<((Map<String, String>?) -> Unit)?>(null)
  }
  var currentEditableTextListOnApply by remember {
    mutableStateOf<((List<String>?) -> Unit)?>(null)
  }

  TabbyNavDisplay(
    backStack = backStack,
    entryProvider =
      entryProvider {
        entry<OverrideSettingsRoute.Main> {
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
            onOpenEditableTextMap = { title, initialValues, onApply ->
              currentEditableTextMapOnApply = onApply
              backStack.addIfNotLast(EditableTextMap(title, initialValues))
            },
            onOpenEditableTextList = { title, initialValues, onApply ->
              currentEditableTextListOnApply = onApply
              backStack.addIfNotLast(EditableTextList(title, initialValues?.toSet()))
            },
          )
        }
        editableTextMapScreenEntry(
          onDismiss = {
            currentEditableTextMapOnApply = null
            backStack.removeLastOrNull()
          },
          onApply = { newValues ->
            currentEditableTextMapOnApply?.invoke(newValues)
            currentEditableTextMapOnApply = null
            backStack.removeLastOrNull()
          },
        )
        editableTextSetScreenEntry(
          onDismiss = {
            currentEditableTextListOnApply = null
            backStack.removeLastOrNull()
          },
          onApply = { newValues ->
            currentEditableTextListOnApply?.invoke(newValues?.toList())
            currentEditableTextListOnApply = null
            backStack.removeLastOrNull()
          },
        )
      },
  )
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
