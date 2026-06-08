package com.github.kr328.clash.home.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource as androidStringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.engine.android.AndroidEngineController
import com.github.kr328.clash.engine.android.AndroidProfileRepository
import com.github.kr328.clash.engine.android.VpnPermissionRequiredException
import com.github.kr328.clash.engine.api.EngineController
import com.github.kr328.clash.engine.api.ProfileRepository
import com.github.kr328.clash.glue.remote.Broadcasts
import com.github.kr328.clash.glue.remote.Remote
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tabby.ui.home.generated.resources.Res as HomeRes
import tabby.ui.home.generated.resources.no_profile_selected
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.profiles

@Composable
internal fun HomeScreen(
  modifier: Modifier = Modifier,
  onOpenProxy: () -> Unit,
  onOpenProfiles: () -> Unit,
  onOpenProviders: () -> Unit,
  onOpenLogs: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenHelp: () -> Unit,
) {
  val context = LocalContext.current
  val appContext = context.applicationContext
  val lifecycleOwner = LocalLifecycleOwner.current
  val scope = rememberCoroutineScope()
  val engineControllerScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
  val engineController: EngineController =
    remember(appContext, engineControllerScope) {
      AndroidEngineController(appContext, engineControllerScope)
    }
  val profileRepository: ProfileRepository = remember { AndroidProfileRepository() }
  val clashRunning by Remote.broadcasts.clashRunningFlow.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  var uiState by remember { mutableStateOf(homeInitialUiState()) }
  var eventState by remember { mutableStateOf<HomeEventState<Intent>>(homeInitialEventState()) }
  var started by remember { mutableStateOf(false) }
  var fetchRequest by remember { mutableIntStateOf(0) }
  val currentFetchRequest = rememberUpdatedState(fetchRequest)

  val noProfileText = stringResource(HomeRes.string.no_profile_selected)
  val profilesActionText = stringResource(SharedRes.string.profiles)

  suspend fun fetchHomeState(clashRunningSnapshot: Boolean) {
    val state = engineController.queryState()
    val providers = engineController.queryProviders()
    val mode = homeModeLabel(state.mode).stringValue(appContext)
    val profileName = profileRepository.queryActive()?.name

    uiState =
      uiState.withFetchedHomeState(
        homeFetchedPlatformState(
          clashRunning = clashRunningSnapshot,
          mode = mode,
          hasProviders = providers.isNotEmpty(),
          profileName = profileName,
        )
      )
  }

  suspend fun startEngine() {
    val result =
      try {
        engineController.start()
        homeEngineStartedResult()
      } catch (e: VpnPermissionRequiredException) {
        homeEngineVpnPermissionResult(e.prepareIntent)
      } catch (e: Exception) {
        Log.e("Start clash service failed: ${e.message}", e)
        homeEngineStartFailedResult(appContext.getString(CommonR.string.unable_to_start_vpn))
      }

    homeEngineStartEventState(result)?.let { eventState = it }
  }

  suspend fun startClash() {
    val action = homeStartAction(profileRepository.queryActive())
    val event = homeStartEventState(action)

    if (event != null) eventState = event else startEngine()
  }

  val vpnLauncher =
    rememberLauncherForActivityResult(StartActivityForResult()) { result ->
      when (
        homeVpnPermissionResultActionFromPlatformResult(
          result = result.resultCode.toHomeVpnPermissionPlatformResult()
        )
      ) {
        HomeVpnPermissionResultAction.StartEngine -> scope.launch { startEngine() }
        HomeVpnPermissionResultAction.Ignore -> Unit
      }
    }

  DisposableEffect(engineControllerScope) { onDispose { engineControllerScope.cancel() } }

  DisposableEffect(lifecycleOwner) {
    started = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    val observer = LifecycleEventObserver { _, event ->
      started =
        homeStartedStateFromPlatformLifecycleEvent(
          currentStarted = started,
          event = event.toHomePlatformStartStopEvent(),
        )
    }

    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  LaunchedEffect(started) {
    when (homeActiveFetchAction(active = started)) {
      HomeActiveFetchAction.RequestFetch -> {
        fetchRequest = currentFetchRequest.value + 1
        Remote.broadcasts.event.collect { event ->
          val action = homeBroadcastAction(event.toHomeBroadcastEvent())

          homeBroadcastEventState(action)?.let { eventState = it }
          if (action.shouldFetch) fetchRequest = currentFetchRequest.value + 1
        }
      }
      HomeActiveFetchAction.Ignore -> Unit
    }
  }

  LaunchedEffect(started, fetchRequest, clashRunning, engineController, profileRepository) {
    when (homeActiveFetchAction(active = started)) {
      HomeActiveFetchAction.RequestFetch -> fetchHomeState(clashRunning)
      HomeActiveFetchAction.Ignore -> Unit
    }
  }

  LaunchedEffect(started, clashRunning, engineController) {
    when (homeTrafficPollAction(started = started, clashRunning = clashRunning)) {
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
    eventState = homeConsumedEventState()
  }

  HomeStateRouteContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    appName = androidStringResource(CommonR.string.tabby),
    logoPainter = painterResource(CommonR.drawable.ic_tabby_foreground),
    clashRunning = clashRunning,
    state = uiState,
    onToggleStatus = {
      scope.launch {
        when (homeToggleAction(clashRunning)) {
          HomeToggleAction.StartClash -> startClash()
          HomeToggleAction.StopClash -> engineController.stop()
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

private fun Broadcasts.Event.toHomeBroadcastEvent() =
  when (this) {
    Broadcasts.Event.ServiceRecreated ->
      homeBroadcastEventFromPlatformPayload(HomePlatformBroadcastEventKind.ServiceRecreated)
    Broadcasts.Event.Started ->
      homeBroadcastEventFromPlatformPayload(HomePlatformBroadcastEventKind.Started)
    is Broadcasts.Event.Stopped ->
      homeBroadcastEventFromPlatformPayload(
        HomePlatformBroadcastEventKind.Stopped,
        stoppedMessage = cause,
      )
    Broadcasts.Event.ProfileChanged ->
      homeBroadcastEventFromPlatformPayload(HomePlatformBroadcastEventKind.ProfileChanged)
    is Broadcasts.Event.ProfileUpdateCompleted ->
      homeBroadcastEventFromPlatformPayload(HomePlatformBroadcastEventKind.ProfileUpdateCompleted)
    is Broadcasts.Event.ProfileUpdateFailed ->
      homeBroadcastEventFromPlatformPayload(HomePlatformBroadcastEventKind.ProfileUpdateFailed)
    Broadcasts.Event.ProfileLoaded ->
      homeBroadcastEventFromPlatformPayload(HomePlatformBroadcastEventKind.ProfileLoaded)
  }

private fun Lifecycle.Event.toHomePlatformStartStopEvent(): HomePlatformStartStopEvent =
  when (this) {
    Lifecycle.Event.ON_START -> HomePlatformStartStopEvent.Start
    Lifecycle.Event.ON_STOP -> HomePlatformStartStopEvent.Stop
    else -> HomePlatformStartStopEvent.Other
  }

private fun Int.toHomeVpnPermissionPlatformResult(): HomeVpnPermissionPlatformResult =
  if (this == Activity.RESULT_OK) {
    HomeVpnPermissionPlatformResult.Granted
  } else {
    HomeVpnPermissionPlatformResult.Denied
  }

private fun HomeModeLabel.stringValue(context: Context): String =
  context.getString(
    homeModeLabelPlatformToken(
      label = this,
      directMode = CommonR.string.direct_mode,
      globalMode = CommonR.string.global_mode,
      ruleMode = CommonR.string.rule_mode,
    )
  )
