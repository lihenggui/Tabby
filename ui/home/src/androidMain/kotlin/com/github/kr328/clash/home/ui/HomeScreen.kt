package com.github.kr328.clash.home.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

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
  val engineControllerScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
  val engineController: EngineController =
    remember(appContext, engineControllerScope) {
      AndroidEngineController(appContext, engineControllerScope)
    }
  val profileRepository: ProfileRepository = remember { AndroidProfileRepository() }
  val clashRunning by Remote.broadcasts.clashRunningFlow.collectAsStateWithLifecycle()
  var started by remember { mutableStateOf(false) }
  val vpnPermissionResults = remember {
    MutableSharedFlow<HomeVpnPermissionResult>(extraBufferCapacity = 1)
  }
  val broadcastEvents = remember {
    Remote.broadcasts.event.map { event -> event.toHomeBroadcastEvent() }
  }

  val vpnLauncher =
    rememberLauncherForActivityResult(StartActivityForResult()) { result ->
      vpnPermissionResults.tryEmit(
        homeVpnPermissionResultFromPlatformResult(
          resultCode = result.resultCode,
          grantedResultCode = Activity.RESULT_OK,
        )
      )
    }

  DisposableEffect(engineControllerScope) { onDispose { engineControllerScope.cancel() } }

  DisposableEffect(lifecycleOwner) {
    started = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    val observer = LifecycleEventObserver { _, event ->
      started =
        homeStartedStateFromStartStopEvent(
          currentStarted = started,
          isStartEvent = event == Lifecycle.Event.ON_START,
          isStopEvent = event == Lifecycle.Event.ON_STOP,
        )
    }

    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  HomeRuntimeRouteContent(
    modifier = modifier,
    engineController = engineController,
    profileRepository = profileRepository,
    active = started,
    clashRunning = clashRunning,
    broadcastEvents = broadcastEvents,
    vpnPermissionResults = vpnPermissionResults,
    onFetchHomeState = {
      val state = engineController.queryState()
      val providers = engineController.queryProviders()
      val profileName = profileRepository.queryActive()?.name

      homeFetchedStateFromRuntimePayload(
        mode = state.mode,
        hasProviders = providers.isNotEmpty(),
        profileName = profileName,
      )
    },
    onStartEngine = {
      try {
        engineController.start()
        homeEngineStartedResult()
      } catch (e: VpnPermissionRequiredException) {
        homeEngineVpnPermissionResult(e.prepareIntent)
      } catch (e: Exception) {
        Log.e("Start clash service failed: ${e.message}", e)
        homeEngineStartFailedResult()
      }
    },
    onStopEngine = { engineController.stop() },
    onRunningStateChanged = {},
    onRequestVpnPermission = { vpnLauncher.launch(it) },
    logoPainter = painterResource(CommonR.drawable.ic_tabby_foreground),
    onOpenProxy = onOpenProxy,
    onOpenProfiles = onOpenProfiles,
    onOpenProviders = onOpenProviders,
    onOpenLogs = onOpenLogs,
    onOpenSettings = onOpenSettings,
    onOpenHelp = onOpenHelp,
  )
}

private fun Broadcasts.Event.toHomeBroadcastEvent() =
  homeBroadcastEventFromPlatformPayload(
    event = this,
    kind = Broadcasts.Event::homeBroadcastSourceEventKind,
    stoppedMessage = Broadcasts.Event::homeStoppedMessage,
  )

private fun Broadcasts.Event.homeBroadcastSourceEventKind() =
  when (this) {
    Broadcasts.Event.ServiceRecreated -> HomeBroadcastSourceEventKind.ServiceRecreated
    Broadcasts.Event.Started -> HomeBroadcastSourceEventKind.Started
    is Broadcasts.Event.Stopped -> HomeBroadcastSourceEventKind.Stopped
    Broadcasts.Event.ProfileChanged -> HomeBroadcastSourceEventKind.ProfileChanged
    is Broadcasts.Event.ProfileUpdateCompleted ->
      HomeBroadcastSourceEventKind.ProfileUpdateCompleted
    is Broadcasts.Event.ProfileUpdateFailed -> HomeBroadcastSourceEventKind.ProfileUpdateFailed
    Broadcasts.Event.ProfileLoaded -> HomeBroadcastSourceEventKind.ProfileLoaded
  }

private fun Broadcasts.Event.homeStoppedMessage() =
  when (this) {
    is Broadcasts.Event.Stopped -> cause
    else -> null
  }
