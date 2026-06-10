package com.github.kr328.clash.crash.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.crash.model.AndroidCrashLogRepository

@Composable
internal fun AppCrashedScreen(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val application = context.applicationContext as Application
  val crashLogRepository = remember(application) { AndroidCrashLogRepository(application) }

  CrashLogRepositoryAppCrashedRouteContent(
    crashLogRepository = crashLogRepository,
    modifier = modifier,
    onActionError = { cause -> Log.e("Failed to load crash logs", cause) },
  )
}
