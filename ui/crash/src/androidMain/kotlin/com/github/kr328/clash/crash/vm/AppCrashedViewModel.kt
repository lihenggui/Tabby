package com.github.kr328.clash.crash.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.crash.ui.AppCrashLogDumpResult
import com.github.kr328.clash.crash.ui.appCrashLogDumpResult
import com.github.kr328.clash.crash.ui.formatAppCrashLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

internal class AppCrashedViewModel(app: Application) : AndroidViewModel(app) {
  val logs: StateFlow<String> =
    flow {
        val log =
          runCatching {
              val packageInfo =
                application.packageManager.getPackageInfo(application.packageName, 0)
              Log.i(
                "App version: versionName = ${packageInfo.versionName} versionCode = ${packageInfo.longVersionCode}"
              )
              dumpCrash()
            }
            .getOrElse { e ->
              Log.e("Failed to load crash logs", e)
              "Failed to load crash logs: ${e.stackTraceToString()}"
            }
        emit(log)
      }
      .stateIn(viewModelScope, SharingStarted.Lazily, "")

  private suspend fun dumpCrash(): String =
    withContext(Dispatchers.IO) {
      val process = ProcessBuilder(*crashDumpCommand).redirectErrorStream(true).start()
      val result = formatAppCrashLog(process.inputStream.bufferedReader().readLines())
      val exitCode = process.waitFor()

      when (val dumpResult = appCrashLogDumpResult(exitCode, result)) {
        is AppCrashLogDumpResult.UseLog -> dumpResult.log
        is AppCrashLogDumpResult.LogcatFailed -> error(dumpResult.message)
      }
    }
}

private val crashDumpCommand =
  arrayOf("logcat", "-d", "-s", "Go", "DEBUG", "AndroidRuntime", "Tabby", "LwIP")
