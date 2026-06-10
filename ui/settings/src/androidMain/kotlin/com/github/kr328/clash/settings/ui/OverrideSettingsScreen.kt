package com.github.kr328.clash.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.engine.android.AndroidEngineController
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper

@Composable
internal fun OverrideSettingsScreen(
  modifier: Modifier = Modifier,
  onResetCompleted: () -> Unit,
) {
  val context = LocalContext.current
  val appContext = context.applicationContext
  val engineController = remember(appContext) { AndroidEngineController(appContext) }

  EngineControllerOverrideSettingsRouteContent(
    engineController = engineController,
    onResetCompleted = onResetCompleted,
    modifier = modifier,
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
