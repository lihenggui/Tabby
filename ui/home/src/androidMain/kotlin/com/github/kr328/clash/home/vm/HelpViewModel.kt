package com.github.kr328.clash.home.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.di.AppInfoProvider.Companion.instance as appInfoProvider
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.bridge.Bridge
import com.github.kr328.clash.glue.util.TABBY_RELEASES_LATEST
import com.github.kr328.clash.home.R
import com.github.kr328.clash.home.api.HelpApi
import com.github.kr328.clash.home.ui.HelpContentState
import com.github.kr328.clash.home.ui.HelpEventState
import com.github.kr328.clash.home.ui.HelpUpdateCheckRequestAction
import com.github.kr328.clash.home.ui.formatAppVersionInfo
import com.github.kr328.clash.home.ui.helpUpdateCheckAction
import com.github.kr328.clash.home.ui.helpUpdateCheckEventState
import com.github.kr328.clash.home.ui.helpUpdateCheckFailureEventState
import com.github.kr328.clash.home.ui.helpUpdateCheckRequestAction
import com.github.kr328.clash.home.ui.withUpdateCheckFinished
import com.github.kr328.clash.home.ui.withUpdateCheckStarted
import com.github.kr328.clash.home.ui.withVersionInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class HelpViewModel(app: Application) : AndroidViewModel(app) {
  private val api = HelpApi()

  val uiState: StateFlow<HelpContentState>
    field = MutableStateFlow(HelpContentState())

  val eventState: StateFlow<HelpEventState>
    field = MutableStateFlow<HelpEventState>(HelpEventState.Idle)

  init {
    loadVersionInfo()
  }

  fun checkForUpdates() {
    when (helpUpdateCheckRequestAction(uiState.value)) {
      HelpUpdateCheckRequestAction.StartCheck -> Unit
      HelpUpdateCheckRequestAction.Ignore -> return
    }

    viewModelScope.launch {
      uiState.update { it.withUpdateCheckStarted() }
      try {
        val latestTag = api.getLatestRelease()
        val action =
          if (latestTag == null) {
            helpUpdateCheckAction(latestTag = null, localVersion = "")
          } else {
            val localVersion =
              application.packageManager.getPackageInfo(application.packageName, 0).versionName
                ?: ""
            helpUpdateCheckAction(latestTag = latestTag, localVersion = localVersion)
          }

        eventState.update {
          helpUpdateCheckEventState(
            action = action,
            releasesUrl = TABBY_RELEASES_LATEST,
            alreadyUpToDateMessage = application.getString(R.string.already_up_to_date),
            updateCheckFailedMessage = application.getString(R.string.check_update_failed),
          )
        }
      } catch (e: Exception) {
        Log.e("Check for updates failed: ${e.message}", e)
        eventState.update {
          helpUpdateCheckFailureEventState(application.getString(R.string.check_update_failed))
        }
      } finally {
        uiState.update { it.withUpdateCheckFinished() }
      }
    }
  }

  fun consumeEvent() {
    eventState.value = HelpEventState.Idle
  }

  private fun loadVersionInfo() {
    viewModelScope.launch {
      val (appVersion, coreVersion) =
        withContext(Dispatchers.IO) {
          val pkgInfo = application.packageManager.getPackageInfo(application.packageName, 0)
          formatAppVersionInfo(
            versionName = pkgInfo.versionName,
            buildCommit = appInfoProvider.buildCommit,
          ) to Bridge.nativeCoreVersion()
        }

      uiState.update { it.withVersionInfo(appVersion = appVersion, coreVersion = coreVersion) }
    }
  }
}
