package com.github.kr328.clash.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.github.kr328.clash.ui.nav.TabbyNavDisplay
import com.github.kr328.clash.ui.nav.addIfNotLast
import kotlinx.serialization.Serializable

internal sealed interface OverrideSettingsRoute : NavKey {
  @Serializable data object Main : OverrideSettingsRoute
}

@Composable
internal fun OverrideSettingsNavigatorContent(
  mainContent:
    @Composable
    (
      onOpenEditableTextMap:
        (EditableTextTitle, Map<String, String>?, (Map<String, String>?) -> Unit) -> Unit,
      onOpenEditableTextList: (EditableTextTitle, List<String>?, (List<String>?) -> Unit) -> Unit,
    ) -> Unit
) {
  val backStack = remember { mutableStateListOf<NavKey>(OverrideSettingsRoute.Main) }
  var currentEditableTextMapOnApply by remember {
    mutableStateOf<((Map<String, String>?) -> Unit)?>(null)
  }
  var currentEditableTextListOnApply by remember {
    mutableStateOf<((List<String>?) -> Unit)?>(null)
  }

  TabbyNavDisplay(
    backStack = backStack,
    entryProvider =
      entryProvider<NavKey> {
        entry<OverrideSettingsRoute.Main> {
          mainContent(
            { title, initialValues, onApply ->
              currentEditableTextMapOnApply = onApply
              backStack.addIfNotLast(EditableTextMap(title, initialValues))
            },
            { title, initialValues, onApply ->
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
