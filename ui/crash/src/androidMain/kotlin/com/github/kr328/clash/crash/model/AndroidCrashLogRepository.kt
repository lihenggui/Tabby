package com.github.kr328.clash.crash.model

import android.app.Application
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.crash.ui.AppCrashLogLoadAction
import com.github.kr328.clash.crash.ui.appCrashLogLoadAction
import com.github.kr328.clash.crash.ui.formatAppCrashLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AndroidCrashLogRepository(private val application: Application) :
  CrashLogRepository {
  override suspend fun loadCrashLog(): String =
    withContext(Dispatchers.IO) {
      val packageInfo = application.packageManager.getPackageInfo(application.packageName, 0)
      Log.i(
        "App version: versionName = ${packageInfo.versionName} versionCode = ${packageInfo.longVersionCode}"
      )

      val process = ProcessBuilder(*crashDumpCommand).redirectErrorStream(true).start()
      val result = formatAppCrashLog(process.inputStream.bufferedReader().readLines())
      val exitCode = process.waitFor()

      when (val action = appCrashLogLoadAction(exitCode, result)) {
        is AppCrashLogLoadAction.UseLog -> action.log
        is AppCrashLogLoadAction.Fail -> error(action.message)
      }
    }
}

private val crashDumpCommand =
  arrayOf("logcat", "-d", "-s", "Go", "DEBUG", "AndroidRuntime", "Tabby", "LwIP")
