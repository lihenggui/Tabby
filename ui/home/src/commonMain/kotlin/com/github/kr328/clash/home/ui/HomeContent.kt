package com.github.kr328.clash.home.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.ui.component.Spacer
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineApps
import com.github.kr328.clash.ui.icon.BaselineAssignment
import com.github.kr328.clash.ui.icon.BaselineHelpCenter
import com.github.kr328.clash.ui.icon.BaselineSettings
import com.github.kr328.clash.ui.icon.BaselineSwapVerticalCircle
import com.github.kr328.clash.ui.icon.BaselineViewList
import com.github.kr328.clash.ui.icon.OutlineCheckCircle
import com.github.kr328.clash.ui.icon.OutlineNotInterested
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.theme.TabbyDarkSurface
import com.github.kr328.clash.ui.theme.TabbyLightStopped
import com.github.kr328.clash.ui.theme.TabbyOnPrimary
import org.jetbrains.compose.resources.stringResource
import tabby.ui.home.generated.resources.Res as HomeRes
import tabby.ui.home.generated.resources.format_profile_activated
import tabby.ui.home.generated.resources.format_traffic_forwarded
import tabby.ui.home.generated.resources.help
import tabby.ui.home.generated.resources.profile
import tabby.ui.home.generated.resources.stopped
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.logs
import tabby.ui.shared.generated.resources.not_selected
import tabby.ui.shared.generated.resources.providers
import tabby.ui.shared.generated.resources.proxy
import tabby.ui.shared.generated.resources.running
import tabby.ui.shared.generated.resources.settings
import tabby.ui.shared.generated.resources.tap_to_start

@Composable
internal fun HomeContent(
  snackbarHostState: SnackbarHostState,
  appName: String,
  logoPainter: Painter,
  clashRunning: Boolean,
  forwarded: String?,
  mode: String?,
  profileName: String?,
  hasProviders: Boolean,
  onToggleStatus: () -> Unit,
  onOpenProxy: () -> Unit,
  onOpenProfiles: () -> Unit,
  onOpenProviders: () -> Unit,
  onOpenLogs: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenHelp: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val darkTheme = isSystemInDarkTheme()
  val stoppedColor = if (darkTheme) TabbyDarkSurface else TabbyLightStopped

  TabbyScaffold(
    title = "",
    modifier = modifier,
    topBar = {},
    snackbarHostState = snackbarHostState,
  ) { innerPadding ->
    Column(
      modifier =
        Modifier.fillMaxSize()
          .padding(innerPadding)
          .padding(horizontal = 30.dp)
          .verticalScroll(rememberScrollState())
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().height(90.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Spacer(10.dp)
        Image(painter = logoPainter, contentDescription = null, modifier = Modifier.size(75.dp))
        Spacer(10.dp)
        Text(text = appName, style = MaterialTheme.typography.headlineLarge)
      }

      HomeActionCard(
        modifier = Modifier.padding(vertical = cardMarginVertical),
        icon = if (clashRunning) TabbyIcons.OutlineCheckCircle else TabbyIcons.OutlineNotInterested,
        text =
          stringResource(if (clashRunning) SharedRes.string.running else HomeRes.string.stopped),
        subtext =
          if (clashRunning && forwarded != null)
            stringResource(HomeRes.string.format_traffic_forwarded, forwarded)
          else stringResource(SharedRes.string.tap_to_start),
        backgroundColor = if (clashRunning) MaterialTheme.colorScheme.primary else stoppedColor,
        contentColor = TabbyOnPrimary,
        onClick = onToggleStatus,
      )

      AnimatedVisibility(visible = clashRunning) {
        HomeActionCard(
          modifier = Modifier.padding(vertical = cardMarginVertical),
          icon = TabbyIcons.BaselineApps,
          text = stringResource(SharedRes.string.proxy),
          subtext = mode,
          backgroundColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.onSurface,
          onClick = onOpenProxy,
        )
      }

      HomeActionCard(
        modifier = Modifier.padding(vertical = cardMarginVertical),
        icon = TabbyIcons.BaselineViewList,
        text = stringResource(HomeRes.string.profile),
        subtext =
          if (profileName != null)
            stringResource(HomeRes.string.format_profile_activated, profileName)
          else stringResource(SharedRes.string.not_selected),
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        onClick = onOpenProfiles,
      )

      AnimatedVisibility(visible = clashRunning && hasProviders) {
        HomeActionLabel(
          modifier = Modifier.padding(vertical = labelMarginVertical),
          icon = TabbyIcons.BaselineSwapVerticalCircle,
          text = stringResource(SharedRes.string.providers),
          onClick = onOpenProviders,
        )
      }

      HomeActionLabel(
        modifier = Modifier.padding(vertical = labelMarginVertical),
        icon = TabbyIcons.BaselineAssignment,
        text = stringResource(SharedRes.string.logs),
        onClick = onOpenLogs,
      )
      HomeActionLabel(
        modifier = Modifier.padding(vertical = labelMarginVertical),
        icon = TabbyIcons.BaselineSettings,
        text = stringResource(SharedRes.string.settings),
        onClick = onOpenSettings,
      )
      HomeActionLabel(
        modifier = Modifier.padding(vertical = labelMarginVertical),
        icon = TabbyIcons.BaselineHelpCenter,
        text = stringResource(HomeRes.string.help),
        onClick = onOpenHelp,
      )
    }
  }
}

@Composable
private fun HomeActionCard(
  icon: ImageVector,
  text: String,
  subtext: String?,
  backgroundColor: Color,
  contentColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth().heightIn(min = 85.dp),
    onClick = onClick,
    colors = CardDefaults.cardColors(containerColor = backgroundColor, contentColor = contentColor),
    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
  ) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = actionItemPaddingHorizontal, vertical = actionItemPaddingVertical),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(actionIconSize),
        tint = contentColor,
      )
      Spacer(actionItemPaddingHorizontal)
      Column {
        Text(text = text, style = MaterialTheme.typography.bodyLarge, color = contentColor)
        if (subtext != null) {
          Spacer(5.dp)
          Text(text = subtext, style = MaterialTheme.typography.bodyMedium, color = contentColor)
        }
      }
    }
  }
}

@Composable
private fun HomeActionLabel(
  icon: ImageVector,
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = 60.dp)
        .clickable(onClick = onClick)
        .padding(vertical = actionItemPaddingVertical),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(actionItemPaddingHorizontal)
    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(actionIconSize))
    Spacer(actionItemPaddingHorizontal)
    Text(text = text, style = MaterialTheme.typography.bodyLarge)
  }
}

private val cardMarginVertical = 5.dp
private val labelMarginVertical = 2.dp
private val actionItemPaddingHorizontal = 20.dp
private val actionItemPaddingVertical = 15.dp
private val actionIconSize = 30.dp
