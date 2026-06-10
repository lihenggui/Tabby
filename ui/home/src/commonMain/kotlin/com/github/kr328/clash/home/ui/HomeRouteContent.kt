package com.github.kr328.clash.home.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.github.kr328.clash.engine.api.EngineController
import com.github.kr328.clash.engine.api.ProfileRepository
import com.github.kr328.clash.ui.icon.BaselineSwapVerticalCircle
import com.github.kr328.clash.ui.icon.TabbyIcons
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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
import tabby.ui.shared.generated.resources.unable_to_start_vpn
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
  var clashRunning by remember { mutableStateOf(false) }
  val engineState by engineController.state.collectAsState()
  HomeRuntimeRouteContent<Nothing>(
    modifier = modifier,
    engineController = engineController,
    profileRepository = profileRepository,
    active = true,
    clashRunning = clashRunning,
    broadcastEvents = emptyFlow(),
    vpnPermissionResults = emptyFlow(),
    refreshKey = engineState.mode,
    onFetchHomeState = { clashRunningSnapshot ->
      val state = runCatching { engineController.queryState() }.getOrDefault(engineState)
      val profileName = runCatching { profileRepository.queryActive()?.name }.getOrNull()
      val hasProviders =
        if (clashRunningSnapshot) {
          runCatching { engineController.queryProviders().isNotEmpty() }.getOrDefault(false)
        } else {
          false
        }

      homeFetchedStateFromRuntimePayload(
        mode = state.mode,
        hasProviders = hasProviders,
        profileName = profileName,
      )
    },
    onStartEngine = {
      runCatching { engineController.start() }
        .fold(
          onSuccess = { homeEngineStartedResult() },
          onFailure = { homeEngineStartFailedResult(it.message) },
        )
    },
    onStopEngine = { engineController.stop() },
    onRunningStateChanged = { clashRunning = it },
    onRequestVpnPermission = {},
    appName = appName,
    onOpenProxy = onOpenProxy,
    onOpenProfiles = onOpenProfiles,
    onOpenProviders = onOpenProviders,
    onOpenLogs = onOpenLogs,
    onOpenSettings = onOpenSettings,
    onOpenHelp = onOpenHelp,
  )
}

@Composable
internal fun <VpnPermissionT> HomeRuntimeRouteContent(
  engineController: EngineController,
  profileRepository: ProfileRepository,
  active: Boolean,
  clashRunning: Boolean,
  broadcastEvents: Flow<HomeBroadcastEvent>,
  vpnPermissionResults: Flow<HomeVpnPermissionResult>,
  onFetchHomeState: suspend (clashRunning: Boolean) -> HomeFetchedState,
  onStartEngine: suspend () -> HomeEngineStartResult<VpnPermissionT>,
  onStopEngine: suspend () -> Unit,
  onRunningStateChanged: (Boolean) -> Unit,
  onRequestVpnPermission: (VpnPermissionT) -> Unit,
  onOpenProxy: () -> Unit,
  onOpenProfiles: () -> Unit,
  onOpenProviders: () -> Unit,
  onOpenLogs: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenHelp: () -> Unit,
  modifier: Modifier = Modifier,
  appName: String = "Tabby",
  logoPainter: Painter = rememberVectorPainter(TabbyIcons.BaselineSwapVerticalCircle),
  refreshKey: Any? = Unit,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  var uiState by remember { mutableStateOf(homeInitialUiState()) }
  var eventState by remember {
    mutableStateOf<HomeEventState<VpnPermissionT>>(homeInitialEventState())
  }
  var fetchRequest by remember { mutableIntStateOf(0) }

  val directMode = stringResource(SharedRes.string.direct_mode)
  val globalMode = stringResource(SharedRes.string.global_mode)
  val ruleMode = stringResource(SharedRes.string.rule_mode)
  val noProfileMessage = stringResource(HomeRes.string.no_profile_selected)
  val profilesAction = stringResource(SharedRes.string.profiles)
  val engineStartFailureMessage = stringResource(SharedRes.string.unable_to_start_vpn)
  val fallbackFailureMessage = stringResource(SharedRes.string.unavailable)

  suspend fun refreshHomeState(clashRunningSnapshot: Boolean) {
    val fetchedState = onFetchHomeState(clashRunningSnapshot)

    uiState =
      uiState.withFetchedHomeState(
        clashRunning = clashRunningSnapshot,
        fetchedState = fetchedState,
        directMode = directMode,
        globalMode = globalMode,
        ruleMode = ruleMode,
      )
  }

  suspend fun startEngine() {
    val result = onStartEngine()

    if (result == HomeEngineStartResult.Started) {
      onRunningStateChanged(true)
      fetchRequest += 1
    }

    homeEngineStartEventState(result, fallbackMessage = engineStartFailureMessage)?.let {
      eventState = it
    }
  }

  suspend fun startClash() {
    val action = homeStartAction(profileRepository.queryActive())
    val event = homeStartEventState(action)

    if (event != null) eventState = event else startEngine()
  }

  suspend fun stopClash() {
    runCatching { onStopEngine() }
      .onSuccess {
        onRunningStateChanged(false)
        fetchRequest += 1
      }
      .onFailure { eventState = homeStartFailureEventState(it.message ?: fallbackFailureMessage) }
  }

  LaunchedEffect(active, broadcastEvents) {
    when (homeActiveFetchAction(active = active)) {
      HomeActiveFetchAction.RequestFetch -> {
        fetchRequest += 1
        broadcastEvents.collect { event ->
          val action = homeBroadcastAction(event)

          homeBroadcastEventState(action)?.let { eventState = it }
          if (action.shouldFetch) fetchRequest += 1
        }
      }
      HomeActiveFetchAction.Ignore -> Unit
    }
  }

  LaunchedEffect(
    active,
    fetchRequest,
    clashRunning,
    engineController,
    profileRepository,
    refreshKey,
    directMode,
    globalMode,
    ruleMode,
  ) {
    when (homeActiveFetchAction(active = active)) {
      HomeActiveFetchAction.RequestFetch -> refreshHomeState(clashRunning)
      HomeActiveFetchAction.Ignore -> Unit
    }
  }

  LaunchedEffect(active, clashRunning, engineController) {
    when (homeTrafficPollAction(started = active, clashRunning = clashRunning)) {
      HomeTrafficPollAction.QueryTraffic -> {
        while (isActive) {
          delay(1.seconds)
          val total = engineController.queryTraffic()
          uiState = uiState.withForwardedTraffic(homeTrafficTotalText(total))
        }
      }
      HomeTrafficPollAction.Ignore -> Unit
    }
  }

  LaunchedEffect(vpnPermissionResults) {
    vpnPermissionResults.collect { result ->
      when (homeVpnPermissionResultAction(result)) {
        HomeVpnPermissionResultAction.StartEngine -> startEngine()
        HomeVpnPermissionResultAction.Ignore -> Unit
      }
    }
  }

  LaunchedEffect(eventState) {
    when (val action = homeEventRouteEffect(eventState)) {
      HomeEventRouteEffect.Ignore -> Unit
      is HomeEventRouteEffect.RequestVpnPermission ->
        onRequestVpnPermission(action.permissionRequest)
      HomeEventRouteEffect.ShowNoProfileMessage -> {
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
      is HomeEventRouteEffect.ShowMessage -> {
        snackbarHostState.showSnackbar(message = action.message)
      }
    }
    eventState = homeConsumedEventState()
  }

  HomeStateRouteContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    appName = appName,
    logoPainter = logoPainter,
    clashRunning = clashRunning,
    state = uiState,
    onToggleStatus = {
      scope.launch {
        when (homeToggleAction(clashRunning)) {
          HomeToggleAction.StartClash -> startClash()
          HomeToggleAction.StopClash -> stopClash()
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

@Composable
internal fun HomeStateRouteContent(
  clashRunning: Boolean,
  state: HomeUiState,
  onToggleStatus: () -> Unit,
  onOpenProxy: () -> Unit,
  onOpenProfiles: () -> Unit,
  onOpenProviders: () -> Unit,
  onOpenLogs: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenHelp: () -> Unit,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  appName: String = "Tabby",
  logoPainter: Painter = rememberVectorPainter(TabbyIcons.BaselineSwapVerticalCircle),
) {
  HomeContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    appName = appName,
    logoPainter = logoPainter,
    clashRunning = clashRunning,
    forwarded = state.forwarded,
    mode = state.mode,
    profileName = state.profileName,
    hasProviders = state.hasProviders,
    onToggleStatus = onToggleStatus,
    onOpenProxy = onOpenProxy,
    onOpenProfiles = onOpenProfiles,
    onOpenProviders = onOpenProviders,
    onOpenLogs = onOpenLogs,
    onOpenSettings = onOpenSettings,
    onOpenHelp = onOpenHelp,
  )
}
