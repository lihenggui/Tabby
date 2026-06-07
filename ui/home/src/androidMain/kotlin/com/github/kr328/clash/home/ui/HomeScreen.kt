package com.github.kr328.clash.home.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource as androidStringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.home.vm.HomeViewModel
import com.github.kr328.clash.ui.lifecycle.viewModelWithLifecycle
import org.jetbrains.compose.resources.stringResource
import tabby.ui.home.generated.resources.Res as HomeRes
import tabby.ui.home.generated.resources.no_profile_selected
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.profiles

@Composable
internal fun HomeScreen(
  modifier: Modifier = Modifier,
  viewModel: HomeViewModel = viewModelWithLifecycle(),
  onOpenProxy: () -> Unit,
  onOpenProfiles: () -> Unit,
  onOpenProviders: () -> Unit,
  onOpenLogs: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenHelp: () -> Unit,
) {
  val clashRunning by viewModel.clashRunning.collectAsStateWithLifecycle()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val eventState by viewModel.eventState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  val noProfileText = stringResource(HomeRes.string.no_profile_selected)
  val profilesActionText = stringResource(SharedRes.string.profiles)

  val vpnLauncher =
    rememberLauncherForActivityResult(StartActivityForResult()) { result ->
      when (homeVpnPermissionResultAction(result.toHomeVpnPermissionResult())) {
        HomeVpnPermissionResultAction.StartEngine -> viewModel.onVpnPermissionGranted()
        HomeVpnPermissionResultAction.Ignore -> Unit
      }
    }

  LaunchedEffect(eventState) {
    when (val action = homeEventPlatformAction(eventState)) {
      HomeEventPlatformAction.Ignore -> Unit
      is HomeEventPlatformAction.RequestVpnPermission ->
        vpnLauncher.launch(action.permissionRequest)
      HomeEventPlatformAction.ShowNoProfileMessage -> {
        val result =
          snackbarHostState.showSnackbar(
            message = noProfileText,
            actionLabel = profilesActionText,
            duration = SnackbarDuration.Long,
          )

        when (homeNoProfileSnackbarAction(result.toSnackbarActionResult())) {
          HomeNoProfileSnackbarAction.OpenProfiles -> onOpenProfiles()
          HomeNoProfileSnackbarAction.Ignore -> Unit
        }
      }
      is HomeEventPlatformAction.ShowMessage -> {
        snackbarHostState.showSnackbar(message = action.message)
      }
    }
    viewModel.consumeEvent()
  }

  HomeContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    appName = androidStringResource(CommonR.string.tabby),
    logoPainter = painterResource(CommonR.drawable.ic_tabby_foreground),
    clashRunning = clashRunning,
    forwarded = uiState.forwarded,
    mode = uiState.mode,
    profileName = uiState.profileName,
    hasProviders = uiState.hasProviders,
    onToggleStatus = viewModel::toggleStatus,
    onOpenProxy = onOpenProxy,
    onOpenProfiles = onOpenProfiles,
    onOpenProviders = onOpenProviders,
    onOpenLogs = onOpenLogs,
    onOpenSettings = onOpenSettings,
    onOpenHelp = onOpenHelp,
  )
}
