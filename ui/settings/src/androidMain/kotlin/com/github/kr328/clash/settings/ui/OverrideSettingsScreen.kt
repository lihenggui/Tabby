package com.github.kr328.clash.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
  val configuration by viewModel.configuration.collectAsStateWithLifecycle()

  OverrideSettingsRouteContent(
    onResetCompleted = onResetCompleted,
    modifier = modifier,
    initialConfiguration = configuration,
    onConfigurationChange = viewModel::setConfiguration,
    onReset = viewModel::resetOverride,
  )
}

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun OverrideSettingsContentPreview() {
  OverrideSettingsRouteContent(
    onResetCompleted = {},
    initialConfiguration = ConfigurationOverride(),
  )
}
