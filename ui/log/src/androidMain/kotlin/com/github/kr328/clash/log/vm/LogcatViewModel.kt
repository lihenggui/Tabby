package com.github.kr328.clash.log.vm

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.glue.util.format
import com.github.kr328.clash.glue.util.logsDir
import com.github.kr328.clash.log.LogcatService
import com.github.kr328.clash.log.R
import com.github.kr328.clash.log.model.LogFile
import com.github.kr328.clash.log.ui.LogcatCloseAction
import com.github.kr328.clash.log.ui.LogcatDeleteAction
import com.github.kr328.clash.log.ui.LogcatExportAction
import com.github.kr328.clash.log.ui.LogcatInitialAction
import com.github.kr328.clash.log.ui.LogcatPollAction
import com.github.kr328.clash.log.ui.LogcatRequestExportAction
import com.github.kr328.clash.log.ui.LogcatUiState
import com.github.kr328.clash.log.ui.logcatCloseAction
import com.github.kr328.clash.log.ui.logcatDeleteAction
import com.github.kr328.clash.log.ui.logcatExportAction
import com.github.kr328.clash.log.ui.logcatInitialAction
import com.github.kr328.clash.log.ui.logcatPollAction
import com.github.kr328.clash.log.ui.logcatRequestExportAction
import com.github.kr328.clash.log.ui.logcatSnapshotAction
import com.github.kr328.clash.log.ui.withExportFinished
import com.github.kr328.clash.log.ui.withExportProgress
import com.github.kr328.clash.log.ui.withExportStarted
import com.github.kr328.clash.log.ui.withMessages
import com.github.kr328.clash.log.ui.withStreaming
import com.github.kr328.clash.log.util.LogcatExportWriter
import com.github.kr328.clash.log.util.LogcatReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

internal class LogcatViewModel(app: Application) : AndroidViewModel(app), DefaultLifecycleObserver {
  private var conn: ServiceConnection? = null
  @Suppress("StaticFieldLeak") private var logcat: LogcatService? = null
  private var pollJob: Job? = null
  private var currentFile: LogFile? = null
  private var initialized = false
  private var started = false
  private var initialSnapshot = true

  val uiState: StateFlow<LogcatUiState>
    field = MutableStateFlow(LogcatUiState())

  val eventState: StateFlow<EventState>
    field = MutableStateFlow<EventState>(EventState.Idle)

  fun init(fileName: String?) {
    if (initialized) return
    initialized = true

    when (val action = logcatInitialAction(fileName)) {
      LogcatInitialAction.StartStreaming -> {
        uiState.update { it.withStreaming(true) }
        startStreaming()
      }
      is LogcatInitialAction.LoadFile -> {
        currentFile = action.file
        uiState.update { it.withStreaming(false) }
        loadLocalFile(action.file)
      }
      LogcatInitialAction.InvalidFile -> {
        eventState.value = EventState.InvalidFile
      }
    }
  }

  fun close() {
    eventState.value =
      when (logcatCloseAction(uiState.value)) {
        LogcatCloseAction.StopStreamingAndOpenLogs -> {
          application.stopService(LogcatService::class.intent)
          EventState.OpenLogs
        }
        LogcatCloseAction.CloseViewer -> EventState.Close
      }
  }

  fun delete() {
    when (val action = logcatDeleteAction(currentFile)) {
      is LogcatDeleteAction.DeleteFile ->
        viewModelScope.launch {
          withContext(Dispatchers.IO) { application.logsDir.resolve(action.file.fileName).delete() }
          eventState.value = EventState.Close
        }
      LogcatDeleteAction.Ignore -> Unit
    }
  }

  fun requestExport() {
    when (val action = logcatRequestExportAction(currentFile)) {
      is LogcatRequestExportAction.RequestExport ->
        eventState.value = EventState.RequestExport(action.fileName)
      LogcatRequestExportAction.Ignore -> Unit
    }
  }

  fun exportTo(uri: Uri?) {
    when (val action = logcatExportAction(currentFile, hasDestination = uri != null)) {
      is LogcatExportAction.ExportFile -> {
        val destination = checkNotNull(uri)
        viewModelScope.launch {
          val messages = uiState.value.messages

          eventState.value =
            try {
              writeLogTo(messages, action.file, destination)
              EventState.ShowMessage(application.getString(R.string.file_exported))
            } catch (e: Exception) {
              Log.e("Export log file failed: ${e.message}", e)
              EventState.ShowMessage(e.message ?: application.getString(CommonR.string.unknown))
            }
        }
      }
      LogcatExportAction.Ignore -> Unit
    }
  }

  fun consumeEvent() {
    eventState.value = EventState.Idle
  }

  override fun onStart(owner: LifecycleOwner) {
    started = true
  }

  override fun onStop(owner: LifecycleOwner) {
    started = false
  }

  override fun onCleared() {
    pollJob?.cancel()
    reset()
  }

  private fun loadLocalFile(file: LogFile) {
    viewModelScope.launch {
      val messages =
        try {
          LogcatReader(application, file).use { it.readAll() }
        } catch (e: Exception) {
          Log.e("Fail to read log file ${file.fileName}: ${e.message}", e)
          eventState.value = EventState.InvalidFile
          return@launch
        }

      uiState.update { it.withMessages(messages) }
    }
  }

  private fun startStreaming() {
    application.startForegroundService(LogcatService::class.intent)

    viewModelScope.launch {
      try {
        logcat = bindLogcatService()
        startPolling()
      } catch (e: Exception) {
        Log.e("Bind logcat service failed: ${e.message}", e)
        runCatching { application.stopService(LogcatService::class.intent) }
          .onFailure { ex -> Log.e("Stop logcat service failed: ${ex.message}", ex) }
        reset()
        eventState.value = EventState.OpenLogs
      }
    }
  }

  private fun startPolling() {
    pollJob?.cancel()
    pollJob = viewModelScope.launch {
      while (isActive) {
        when (val action = logcatPollAction(started, initialSnapshot)) {
          is LogcatPollAction.QuerySnapshot -> {
            val snapshot = logcat?.snapshot(action.initialSnapshot)
            uiState.update { current ->
              val snapshotAction =
                logcatSnapshotAction(
                  state = current,
                  initialSnapshot = action.initialSnapshot,
                  messages = snapshot?.messages,
                )
              initialSnapshot = snapshotAction.initialSnapshot
              snapshotAction.state
            }
          }
          LogcatPollAction.Ignore -> Unit
        }
        delay(500.milliseconds)
      }
    }
  }

  private suspend fun bindLogcatService(): LogcatService {
    return suspendCancellableCoroutine { continuation ->
      val connection =
        object : ServiceConnection {
          override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder =
              service
                ?: run {
                  if (!continuation.isActive) {
                    runCatching { application.unbindService(this) }
                      .onFailure { e -> Log.e("Unbind logcat service failed: ${e.message}", e) }
                    if (conn === this) {
                      conn = null
                    }
                    return
                  }
                  runCatching {
                      continuation.resumeWithException(
                        IllegalStateException("Logcat service returned a null binder")
                      )
                    }
                    .onFailure {
                      Log.e("Resume bind failure: ${it.message}", it)
                      runCatching { application.unbindService(this) }
                        .onFailure { e -> Log.e("Unbind logcat service failed: ${e.message}", e) }
                      if (conn === this) {
                        conn = null
                      }
                    }
                  return
                }
            val logcatService = binder.queryLocalInterface("") as LogcatService

            if (!continuation.isActive) {
              runCatching { application.unbindService(this) }
                .onFailure { e -> Log.e("Unbind logcat service failed: ${e.message}", e) }
              if (conn === this) {
                conn = null
              }
              return
            }

            runCatching { continuation.resume(logcatService) }
              .onFailure {
                Log.e("Resume logcat continuation failed: ${it.message}", it)
                runCatching { application.unbindService(this) }
                  .onFailure { e -> Log.e("Unbind logcat service failed: ${e.message}", e) }
                if (conn === this) {
                  conn = null
                }
              }
          }

          override fun onServiceDisconnected(name: ComponentName?) {
            if (conn === this) {
              conn = null
            }
            logcat = null
          }
        }

      conn = connection

      val bound =
        application.bindService(LogcatService::class.intent, connection, Context.BIND_AUTO_CREATE)

      if (!bound) {
        conn = null
        continuation.resumeWithException(IllegalStateException("Failed to bind logcat service"))
        return@suspendCancellableCoroutine
      }

      continuation.invokeOnCancellation {
        runCatching { application.unbindService(connection) }
          .onFailure { e -> Log.e("Unbind canceled logcat service failed: ${e.message}", e) }

        if (conn === connection) {
          conn = null
        }
      }
    }
  }

  private suspend fun writeLogTo(messages: List<LogMessage>, file: LogFile, uri: Uri) =
    withContext(Dispatchers.IO) {
      BufferedWriter(
          OutputStreamWriter(checkNotNull(application.contentResolver.openOutputStream(uri)))
        )
        .use { writer ->
          val filter =
            LogcatExportWriter(
              output = writer,
              formatHeaderTime = { created -> Date(created).format(application) },
              formatMessageTime = { time -> Date(time).format(application, includeDate = false) },
            )

          uiState.update { it.withExportStarted(messages.size) }

          try {
            filter.writeLogFile(file.created, messages) { progress ->
              uiState.update { it.withExportProgress(progress) }
            }
          } finally {
            uiState.update { it.withExportFinished() }
          }
        }
    }

  private fun reset() {
    conn?.let { connection ->
      runCatching { application.unbindService(connection) }
        .onFailure { e -> Log.e("Unbind logcat service failed: ${e.message}", e) }
    }
    conn = null
    logcat = null
  }

  sealed interface EventState {
    data object Idle : EventState

    data object Close : EventState

    data object InvalidFile : EventState

    data object OpenLogs : EventState

    data class RequestExport(val fileName: String) : EventState

    data class ShowMessage(val message: String) : EventState
  }
}
