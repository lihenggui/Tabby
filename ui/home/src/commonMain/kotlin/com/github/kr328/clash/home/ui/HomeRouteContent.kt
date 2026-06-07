package com.github.kr328.clash.home.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.github.kr328.clash.engine.api.EngineController
import com.github.kr328.clash.engine.api.ProfileRepository
import com.github.kr328.clash.ui.icon.BaselineSwapVerticalCircle
import com.github.kr328.clash.ui.icon.TabbyIcons
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tabby.ui.home.generated.resources.Res as HomeRes
import tabby.ui.home.generated.resources.no_profile_selected
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.direct_mode
import tabby.ui.shared.generated.resources.global_mode
import tabby.ui.shared.generated.resources.profiles
import tabby.ui.shared.generated.resources.rule_mode
import tabby.ui.shared.generated.resources.unavailable

@Composable
fun HomeRouteContent(
  engineController: EngineController,
  profileRepository: ProfileRepository,
  onOpenProxy: () -> Unit,
  onOpenProfiles: () -> Unit,
  onOpenProviders: () -> Unit,
  onOpenLogs: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenHelp: () -> Unit,
  modifier: Modifier = Modifier,
  appName: String = "Tabby",
) {
  val engineState by engineController.state.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  var clashRunning by remember { mutableStateOf(false) }
  var uiState by remember { mutableStateOf(homeInitialUiState()) }

  val directMode = stringResource(SharedRes.string.direct_mode)
  val globalMode = stringResource(SharedRes.string.global_mode)
  val ruleMode = stringResource(SharedRes.string.rule_mode)
  val noProfileMessage = stringResource(HomeRes.string.no_profile_selected)
  val profilesAction = stringResource(SharedRes.string.profiles)
  val fallbackFailureMessage = stringResource(SharedRes.string.unavailable)
  val logoPainter = rememberVectorPainter(TabbyIcons.BaselineSwapVerticalCircle)

  suspend fun refreshHomeState() {
    val state = runCatching { engineController.queryState() }.getOrDefault(engineState)
    val profileName = runCatching { profileRepository.queryActive()?.name }.getOrNull()
    val hasProviders =
      if (clashRunning) {
        runCatching { engineController.queryProviders().isNotEmpty() }.getOrDefault(false)
      } else {
        false
      }

    uiState =
      uiState.withFetchedHomeState(
        clashRunning = clashRunning,
        mode =
          homeModeLabelPlatformToken(
            label = homeModeLabel(state.mode),
            directMode = directMode,
            globalMode = globalMode,
            ruleMode = ruleMode,
          ),
        hasProviders = hasProviders,
        profileName = profileName,
      )
  }

  suspend fun showNoProfileMessage() {
    val result =
      snackbarHostState.showSnackbar(
        message = noProfileMessage,
        actionLabel = profilesAction,
        duration = SnackbarDuration.Long,
      )

    when (homeNoProfileSnackbarAction(result.toSnackbarActionResult())) {
      HomeNoProfileSnackbarAction.OpenProfiles -> onOpenProfiles()
      HomeNoProfileSnackbarAction.Ignore -> Unit
    }
  }

  suspend fun startEngine() {
    val activeProfile = runCatching { profileRepository.queryActive() }.getOrNull()

    when (homeStartAction(activeProfile)) {
      HomeStartAction.ShowNoProfileMessage -> showNoProfileMessage()
      HomeStartAction.StartEngine ->
        runCatching { engineController.start() }
          .onSuccess {
            clashRunning = true
            refreshHomeState()
          }
          .onFailure {
            snackbarHostState.showSnackbar(it.message ?: fallbackFailureMessage)
          }
    }
  }

  suspend fun stopEngine() {
    runCatching { engineController.stop() }
      .onSuccess {
        clashRunning = false
        refreshHomeState()
      }
      .onFailure { snackbarHostState.showSnackbar(it.message ?: fallbackFailureMessage) }
  }

  LaunchedEffect(engineController, profileRepository, engineState.mode, clashRunning) {
    refreshHomeState()
  }

  LaunchedEffect(engineController, clashRunning) {
    when (homeTrafficPollAction(clashRunning)) {
      HomeTrafficPollAction.QueryTraffic -> {
        while (isActive) {
          delay(1.seconds)
          runCatching { engineController.queryTraffic() }
            .onSuccess { uiState = uiState.withForwardedTraffic(homeTrafficTotalText(it)) }
        }
      }
      HomeTrafficPollAction.Ignore -> Unit
    }
  }

  HomeContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    appName = appName,
    logoPainter = logoPainter,
    clashRunning = clashRunning,
    forwarded = uiState.forwarded,
    mode = uiState.mode,
    profileName = uiState.profileName,
    hasProviders = uiState.hasProviders,
    onToggleStatus = {
      scope.launch {
        when (homeToggleAction(clashRunning)) {
          HomeToggleAction.StartClash -> startEngine()
          HomeToggleAction.StopClash -> stopEngine()
        }
      }
    },
    onOpenProxy = onOpenProxy,
    onOpenProfiles = onOpenProfiles,
    onOpenProviders = onOpenProviders,
    onOpenLogs = onOpenLogs,
    onOpenSettings = onOpenSettings,
    onOpenHelp = onOpenHelp,
  )
}
