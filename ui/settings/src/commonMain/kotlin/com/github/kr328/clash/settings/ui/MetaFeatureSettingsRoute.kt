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

internal sealed interface MetaFeatureSettingsRoute : NavKey {
  @Serializable data object Main : MetaFeatureSettingsRoute
}

@Composable
internal fun MetaFeatureSettingsNavigatorContent(
  mainContent:
    @Composable
    (
      onOpenEditableTextList: (EditableTextTitle, List<String>?, (List<String>?) -> Unit) -> Unit
    ) -> Unit
) {
  val backStack = remember { mutableStateListOf<NavKey>(MetaFeatureSettingsRoute.Main) }
  var currentEditableTextListOnApply by remember {
    mutableStateOf<((List<String>?) -> Unit)?>(null)
  }

  TabbyNavDisplay(
    backStack = backStack,
    entryProvider =
      entryProvider<NavKey> {
        entry<MetaFeatureSettingsRoute.Main> {
          mainContent { title, initialValues, onApply ->
            currentEditableTextListOnApply = onApply
            backStack.addIfNotLast(EditableTextList(title, initialValues?.toSet()))
          }
        }
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
