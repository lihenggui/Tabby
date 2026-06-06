package com.github.kr328.clash.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.kr328.clash.ui.component.Spacer
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineDns
import com.github.kr328.clash.ui.icon.BaselineExtension
import com.github.kr328.clash.ui.icon.BaselineMihomo
import com.github.kr328.clash.ui.icon.BaselineSettings
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.theme.tabbyDimens
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tabby.ui.settings.generated.resources.Res
import tabby.ui.settings.generated.resources.app
import tabby.ui.settings.generated.resources.meta_features
import tabby.ui.settings.generated.resources.network
import tabby.ui.settings.generated.resources.override
import tabby.ui.settings.generated.resources.settings

@Composable
internal fun SettingsScreen(
  modifier: Modifier = Modifier,
  onOpenAppSettings: () -> Unit,
  onOpenNetworkSettings: () -> Unit,
  onOpenOverrideSettings: () -> Unit,
  onOpenMetaFeatureSettings: () -> Unit,
) {
  TabbyScaffold(title = stringResource(Res.string.settings), modifier = modifier) { innerPadding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())
    ) {
      SettingsEntryItem(
        icon = TabbyIcons.BaselineSettings,
        title = Res.string.app,
        onClick = onOpenAppSettings,
      )

      SettingsEntryItem(
        icon = TabbyIcons.BaselineDns,
        title = Res.string.network,
        onClick = onOpenNetworkSettings,
      )

      SettingsEntryItem(
        icon = TabbyIcons.BaselineExtension,
        title = Res.string.override,
        onClick = onOpenOverrideSettings,
      )

      SettingsEntryItem(
        icon = TabbyIcons.BaselineMihomo,
        title = Res.string.meta_features,
        onClick = onOpenMetaFeatureSettings,
      )
    }
  }
}

@Composable
private fun SettingsEntryItem(
  icon: ImageVector,
  title: StringResource,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val dimens = tabbyDimens
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = dimens.itemMinHeight)
        .clickable(onClick = onClick)
        .padding(
          top = dimens.itemPaddingVertical,
          bottom = dimens.itemPaddingVertical,
          end = dimens.settingsItemEndPadding,
        ),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(dimens.itemHeaderMargin)
    Icon(
      imageVector = icon,
      contentDescription = null,
      modifier = Modifier.size(dimens.itemHeaderComponentSize),
    )
    Spacer(dimens.itemHeaderMargin)
    Text(text = stringResource(title), style = MaterialTheme.typography.bodyLarge)
  }
}
