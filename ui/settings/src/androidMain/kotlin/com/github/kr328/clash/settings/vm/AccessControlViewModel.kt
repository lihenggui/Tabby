package com.github.kr328.clash.settings.vm

import android.Manifest
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Process
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.compat.getInstalledPackagesCompat
import com.github.kr328.clash.core.model.AccessControlSort
import com.github.kr328.clash.glue.model.AppInfo
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.store.UiStore
import com.github.kr328.clash.glue.util.startClashService
import com.github.kr328.clash.glue.util.stopClashService
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.settings.ui.AccessControlClipboardImportPayload
import com.github.kr328.clash.settings.ui.AccessControlUiState
import com.github.kr328.clash.settings.ui.accessControlClipboardImportPayloadFromPlatformPayload
import com.github.kr328.clash.settings.ui.accessControlInitialUiState
import com.github.kr328.clash.settings.ui.accessControlSystemAppFromPlatformFlags
import com.github.kr328.clash.settings.ui.loadAccessControlApps
import com.github.kr328.clash.settings.ui.planAccessControlPersist
import com.github.kr328.clash.settings.ui.withAccessControlApps
import com.github.kr328.clash.settings.ui.withAccessControlReverse
import com.github.kr328.clash.settings.ui.withAccessControlSelectedPackages
import com.github.kr328.clash.settings.ui.withAccessControlShowSystemApps
import com.github.kr328.clash.settings.ui.withAccessControlSort
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

internal class AccessControlViewModel(app: Application) :
  AndroidViewModel(app), DefaultLifecycleObserver {
  private val appContext = app
  private val uiStore = UiStore(app)
  private val serviceStore = ServiceStore(app)
  private var reloadAppsJob: Job? = null

  val uiState: StateFlow<AccessControlUiState<AppInfo>>
    field =
      MutableStateFlow(
        accessControlInitialUiState(
          sort = uiStore.accessControlSort,
          reverse = uiStore.accessControlReverse,
          showSystemApps = uiStore.accessControlSystemApp,
        )
      )

  val clashRunning: StateFlow<Boolean> = Remote.broadcasts.clashRunningFlow

  init {
    viewModelScope.launch {
      val selected = withContext(Dispatchers.IO) { serviceStore.accessControlPackages }
      uiState.update { it.withAccessControlSelectedPackages(selected) }
      reloadApps()
    }
  }

  override fun onStop(owner: LifecycleOwner) {
    // Intended to use non-viewModel scope as we need the action to be called on disposed.
    Global.launch {
      val selected = uiState.value.settings.selected
      val persistedSelection = serviceStore.accessControlPackages
      val persistPlan =
        planAccessControlPersist(
          selected = selected,
          persistedSelection = persistedSelection,
          clashRunning = clashRunning.value,
        )

      if (persistPlan.shouldPersistSelection) {
        serviceStore.accessControlPackages = selected
      }
      if (persistPlan.shouldRestartService) {
        appContext.stopClashService()

        val stopped =
          withTimeoutOrNull(10.seconds) {
            clashRunning.first { !it }
            true
          } ?: false

        if (!stopped) {
          // The stop signal may be lost; continue with a best-effort restart path.
          appContext.stopClashService()
        }

        appContext.startClashService()
      }
    }
  }

  fun setSelectedPackages(selected: Set<String>) {
    uiState.update { it.withAccessControlSelectedPackages(selected) }
  }

  fun importClipboardPayload(): AccessControlClipboardImportPayload {
    val clipboard = appContext.getSystemService<ClipboardManager>()
    val data = clipboard?.primaryClip
    val hasPrimaryClipItem = data != null && data.itemCount > 0
    val clipboardText = if (hasPrimaryClipItem) data.getItemAt(0).text?.toString() else null

    return accessControlClipboardImportPayloadFromPlatformPayload(
      hasPrimaryClipItem = hasPrimaryClipItem,
      clipboardText = clipboardText,
    )
  }

  fun exportClipboardText(text: String) {
    val clipboard = appContext.getSystemService<ClipboardManager>()
    val data = ClipData.newPlainText("packages", text)
    clipboard?.setPrimaryClip(data)
  }

  fun setSort(sort: AccessControlSort) {
    uiStore.accessControlSort = sort
    uiState.update { it.withAccessControlSort(sort) }
    reloadApps()
  }

  fun setReverse(reverse: Boolean) {
    uiStore.accessControlReverse = reverse
    uiState.update { it.withAccessControlReverse(reverse) }
    reloadApps()
  }

  fun setShowSystemApps(show: Boolean) {
    uiStore.accessControlSystemApp = show
    uiState.update { it.withAccessControlShowSystemApps(show) }
    reloadApps()
  }

  private fun reloadApps() {
    reloadAppsJob?.cancel()
    reloadAppsJob = viewModelScope.launch {
      val state = uiState.value.settings
      val apps =
        loadApps(
          selected = state.selected,
          sort = state.sort,
          reverse = state.reverse,
          showSystemApps = state.showSystemApps,
        )

      uiState.update { it.withAccessControlApps(apps) }
    }
  }

  private suspend fun loadApps(
    selected: Set<String>,
    sort: AccessControlSort,
    reverse: Boolean,
    showSystemApps: Boolean,
  ): List<AppInfo> =
    withContext(Dispatchers.IO) {
      val pm = appContext.packageManager
      loadAccessControlApps(
        packages = pm.getInstalledPackagesCompat(PackageManager.GET_PERMISSIONS),
        selectedPackageNames = selected,
        sort = sort,
        reverse = reverse,
        currentPackageName = appContext.packageName,
        showSystemApps = showSystemApps,
        packageName = PackageInfo::packageName,
        hasAppMetadata = { it.applicationInfo != null },
        hasInternetPermission = {
          it.requestedPermissions?.contains(Manifest.permission.INTERNET) == true
        },
        hasSystemUid = {
          it.applicationInfo?.uid?.let { uid -> uid < Process.FIRST_APPLICATION_UID } == true
        },
        isSystemApp = { it.isSystemApp },
        toApp = { it.toAppInfo(pm) },
        appPackageName = AppInfo::packageName,
        appLabel = AppInfo::label,
        appInstallTime = AppInfo::installTime,
        appUpdateTime = AppInfo::updateDate,
      )
    }

  private val PackageInfo.isSystemApp: Boolean
    get() =
      accessControlSystemAppFromPlatformFlags(
        flags = applicationInfo?.flags,
        systemAppFlag = ApplicationInfo.FLAG_SYSTEM,
      )
}

private fun PackageInfo.toAppInfo(pm: PackageManager): AppInfo {
  val applicationInfo = checkNotNull(applicationInfo)
  return AppInfo(
    packageName = packageName,
    icon = applicationInfo.loadIcon(pm).foreground,
    label = applicationInfo.loadLabel(pm).toString(),
    installTime = firstInstallTime,
    updateDate = lastUpdateTime,
  )
}

private val Drawable.foreground: Drawable
  get() {
    if (this is AdaptiveIconDrawable && this.background == null) {
      return this.foreground
    }
    return this
  }
