package com.github.kr328.clash.log.ui

import android.content.ClipData
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.glue.util.format
import com.github.kr328.clash.glue.util.logsDir
import com.github.kr328.clash.log.LogcatService
import com.github.kr328.clash.log.R
import com.github.kr328.clash.log.model.LogFile
import com.github.kr328.clash.log.util.LogcatExportWriter
import com.github.kr328.clash.log.util.LogcatReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import tabby.ui.log.generated.resources.Res as LogRes
import tabby.ui.log.generated.resources.copied
import tabby.ui.log.generated.resources.invalid_log_file

private typealias AndroidLogcatBinding = LogcatServiceBinding<LogcatService, ServiceConnection>

@Composable
internal fun LogcatScreen(
  fileName: String?,
  modifier: Modifier = Modifier,
  onOpenLogs: () -> Unit,
  onInvalidFile: () -> Unit,
  onClose: () -> Unit,
) {
  val clipboard = LocalClipboard.current
  val context = LocalContext.current
  val appContext = context.applicationContext
  val lifecycleOwner = LocalLifecycleOwner.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val messageCopied = stringResource(LogRes.string.copied)
  val invalidFileTip = stringResource(LogRes.string.invalid_log_file)
  val exportedMessage = context.getString(R.string.file_exported)
  val unknownMessage = context.getString(CommonR.string.unknown)
  var uiState by remember { mutableStateOf(logcatInitialUiState()) }
  var eventState by remember { mutableStateOf(logcatInitialEventState()) }
  var currentFile by remember { mutableStateOf<LogFile?>(null) }
  var logcatBinding by remember { mutableStateOf<AndroidLogcatBinding?>(null) }
  var started by remember { mutableStateOf(false) }
  var initialSnapshot by remember { mutableStateOf(true) }
  val currentLogcatBinding = rememberUpdatedState(logcatBinding)

  LaunchedEffect(fileName, appContext) {
    currentLogcatBinding.value?.let { appContext.unbindAndroidLogcat(it) }
    logcatBinding = null
    currentFile = null
    initialSnapshot = true
    uiState = logcatInitialUiState()
    eventState = logcatInitialEventState()

    when (val action = logcatInitialAction(fileName)) {
      LogcatInitialAction.StartStreaming -> {
        uiState = uiState.withInitialAction(action)
        appContext.startForegroundService(LogcatService::class.intent)

        try {
          logcatBinding = appContext.bindAndroidLogcat {
            if (logcatBinding?.connection === it) {
              logcatBinding = null
            }
          }
        } catch (e: Exception) {
          Log.e("Bind logcat service failed: ${e.message}", e)
          runCatching { appContext.stopService(LogcatService::class.intent) }
            .onFailure { ex -> Log.e("Stop logcat service failed: ${ex.message}", ex) }
          eventState = logcatStartStreamingFailureEventState()
        }
      }
      is LogcatInitialAction.LoadFile -> {
        currentFile = action.file
        uiState = uiState.withInitialAction(action)

        try {
          val messages = appContext.loadAndroidLogcatMessages(action.file)
          uiState = uiState.withMessages(messages)
        } catch (e: Exception) {
          Log.e("Fail to read log file ${action.file.fileName}: ${e.message}", e)
          eventState = logcatLoadFileFailureEventState()
        }
      }
      LogcatInitialAction.InvalidFile -> {
        logcatInitialEventState(action)?.let { eventState = it }
      }
    }
  }

  DisposableEffect(lifecycleOwner) {
    started = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    val observer = LifecycleEventObserver { _, event ->
      started =
        logcatStartedStateFromPlatformLifecycleEvent(
          currentStarted = started,
          event = event.toLogcatPlatformStartStopEvent(),
        )
    }

    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  DisposableEffect(appContext) {
    onDispose { currentLogcatBinding.value?.let { appContext.unbindAndroidLogcat(it) } }
  }

  LaunchedEffect(logcatBinding, started) {
    val service = logcatBinding?.service ?: return@LaunchedEffect

    while (isActive) {
      when (val action = logcatPollAction(started, initialSnapshot)) {
        is LogcatPollAction.QuerySnapshot -> {
          val snapshot = service.snapshot(action.initialSnapshot)
          val snapshotAction =
            logcatSnapshotAction(
              state = uiState,
              initialSnapshot = action.initialSnapshot,
              messages = snapshot?.messages,
            )

          initialSnapshot = snapshotAction.initialSnapshot
          uiState = snapshotAction.state
        }
        LogcatPollAction.Ignore -> Unit
      }

      delay(500.milliseconds)
    }
  }

  val exportLauncher =
    rememberLauncherForActivityResult(CreateDocument("text/plain")) { uri ->
      val action = logcatExportActionFromPlatformDestination(currentFile, uri)

      if (action is LogcatExportAction.ExportFile) {
        val destination = checkNotNull(uri)
        scope.launch {
          eventState =
            try {
              appContext.writeAndroidLogTo(
                messages = uiState.messages,
                file = action.file,
                uri = destination,
                onExportStarted = { max -> uiState = uiState.withExportStarted(max) },
                onExportProgress = { progress -> uiState = uiState.withExportProgress(progress) },
                onExportFinished = { uiState = uiState.withExportFinished() },
              )
              logcatExportResultEventState(
                success = true,
                errorMessage = null,
                exportedMessage = exportedMessage,
                unknownMessage = unknownMessage,
              )
            } catch (e: Exception) {
              Log.e("Export log file failed: ${e.message}", e)
              logcatExportResultEventState(
                success = false,
                errorMessage = e.message,
                exportedMessage = exportedMessage,
                unknownMessage = unknownMessage,
              )
            }
        }
      }
    }

  LaunchedEffect(eventState) {
    when (val action = logcatEventPlatformAction(eventState)) {
      LogcatEventPlatformAction.Ignore -> Unit
      LogcatEventPlatformAction.Close -> onClose()
      LogcatEventPlatformAction.InvalidFile -> {
        snackbarHostState.showSnackbar(message = invalidFileTip)
        onInvalidFile()
      }
      LogcatEventPlatformAction.OpenLogs -> onOpenLogs()
      is LogcatEventPlatformAction.RequestExport -> exportLauncher.launch(action.fileName)
      is LogcatEventPlatformAction.ShowMessage -> {
        snackbarHostState.showSnackbar(message = action.message, withDismissAction = true)
      }
    }
    eventState = logcatConsumedEventState()
  }

  LogcatStateRouteContent(
    modifier = modifier,
    state = uiState,
    snackbarHostState = snackbarHostState,
    formatMessageTime = { time ->
      Date(time).format(context, includeDate = false, includeTime = true)
    },
    onClose = {
      val action = logcatCloseAction(uiState)
      if (action == LogcatCloseAction.StopStreamingAndOpenLogs) {
        appContext.stopService(LogcatService::class.intent)
      }
      eventState = logcatCloseEventState(action)
    },
    onDelete = {
      when (val action = logcatDeleteAction(currentFile)) {
        is LogcatDeleteAction.DeleteFile -> {
          scope.launch {
            appContext.deleteAndroidLogcatFile(action.file)
            logcatDeleteEventState(action)?.let { eventState = it }
          }
        }
        LogcatDeleteAction.Ignore -> Unit
      }
    },
    onExport = {
      logcatRequestExportEventState(logcatRequestExportAction(currentFile))?.let {
        eventState = it
      }
    },
    onCopyMessage = { message ->
      scope.launch {
        val payload = logcatCopyMessagePayload(message)
        val clipEntry = ClipData.newPlainText(payload.label, payload.text).toClipEntry()
        clipboard.setClipEntry(clipEntry)
        snackbarHostState.showSnackbar(message = messageCopied, withDismissAction = true)
      }
    },
  )
}

private fun Lifecycle.Event.toLogcatPlatformStartStopEvent(): LogcatPlatformStartStopEvent =
  when (this) {
    Lifecycle.Event.ON_START -> LogcatPlatformStartStopEvent.Start
    Lifecycle.Event.ON_STOP -> LogcatPlatformStartStopEvent.Stop
    else -> LogcatPlatformStartStopEvent.Other
  }

private suspend fun Context.bindAndroidLogcat(
  onDisconnected: (ServiceConnection) -> Unit
): AndroidLogcatBinding = suspendCancellableCoroutine { continuation ->
  lateinit var connection: ServiceConnection

  connection =
    object : ServiceConnection {
      override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder =
          service
            ?: run {
              if (continuation.isActive) {
                continuation.resumeWithException(
                  IllegalStateException("Logcat service returned a null binder")
                )
              }
              runCatching { unbindService(this) }
                .onFailure { e -> Log.e("Unbind logcat service failed: ${e.message}", e) }
              return
            }

        if (continuation.isActive) {
          continuation.resume(
            logcatServiceBindingFromPlatformPayload(
              service = binder.queryLocalInterface("") as LogcatService,
              connection = this,
            )
          )
        } else {
          runCatching { unbindService(this) }
            .onFailure { e -> Log.e("Unbind logcat service failed: ${e.message}", e) }
        }
      }

      override fun onServiceDisconnected(name: ComponentName?) {
        onDisconnected(this)
      }
    }

  if (!bindService(LogcatService::class.intent, connection, Context.BIND_AUTO_CREATE)) {
    continuation.resumeWithException(IllegalStateException("Failed to bind logcat service"))
    return@suspendCancellableCoroutine
  }

  continuation.invokeOnCancellation {
    runCatching { unbindService(connection) }
      .onFailure { e -> Log.e("Unbind canceled logcat service failed: ${e.message}", e) }
  }
}

private fun Context.unbindAndroidLogcat(binding: AndroidLogcatBinding) {
  runCatching { unbindService(binding.connection) }
    .onFailure { e -> Log.e("Unbind logcat service failed: ${e.message}", e) }
}

private suspend fun Context.loadAndroidLogcatMessages(file: LogFile): List<LogMessage> {
  return LogcatReader(this, file).use { it.readAll() }
}

private suspend fun Context.deleteAndroidLogcatFile(file: LogFile) {
  withContext(Dispatchers.IO) { logsDir.resolve(file.fileName).delete() }
}

private suspend fun Context.writeAndroidLogTo(
  messages: List<LogMessage>,
  file: LogFile,
  uri: Uri,
  onExportStarted: (Int) -> Unit,
  onExportProgress: (Int) -> Unit,
  onExportFinished: () -> Unit,
) {
  try {
    onExportStarted(messages.size)
    withContext(Dispatchers.IO) {
      BufferedWriter(OutputStreamWriter(checkNotNull(contentResolver.openOutputStream(uri)))).use {
        writer ->
        val filter =
          LogcatExportWriter(
            output = writer,
            formatHeaderTime = { created -> Date(created).format(this@writeAndroidLogTo) },
            formatMessageTime = { time ->
              Date(time).format(this@writeAndroidLogTo, includeDate = false)
            },
          )

        filter.writeHeader(file.created)
        messages.forEachIndexed { index, message ->
          withContext(Dispatchers.Main.immediate) { onExportProgress(index + 1) }
          filter.writeMessage(message)
        }
      }
    }
  } finally {
    onExportFinished()
  }
}
