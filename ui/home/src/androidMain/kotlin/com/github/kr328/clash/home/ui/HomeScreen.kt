package com.github.kr328.clash.home.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
      if (result.resultCode == Activity.RESULT_OK) {
        viewModel.onVpnPermissionGranted()
      }
    }

  LaunchedEffect(eventState) {
    when (val event = eventState) {
      Idle -> Unit
      is RequestVpnPermission -> vpnLauncher.launch(event.intent)
      ShowNoProfileMessage -> {
        val result =
          snackbarHostState.showSnackbar(
            message = noProfileText,
            actionLabel = profilesActionText,
            duration = SnackbarDuration.Long,
          )

        if (result == SnackbarResult.ActionPerformed) onOpenProfiles()
      }
      is ShowMessage -> {
        snackbarHostState.showSnackbar(message = event.message)
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
