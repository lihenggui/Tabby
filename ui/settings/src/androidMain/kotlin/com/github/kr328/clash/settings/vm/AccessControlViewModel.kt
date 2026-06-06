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
import com.github.kr328.clash.settings.ui.AccessControlActions
import com.github.kr328.clash.settings.ui.AccessControlSettingsState
import com.github.kr328.clash.settings.ui.exportAccessControlPackages
import com.github.kr328.clash.settings.ui.importAccessControlPackages
import com.github.kr328.clash.settings.ui.invertAccessControlPackages
import com.github.kr328.clash.settings.ui.selectAllAccessControlPackages
import com.github.kr328.clash.settings.ui.selectNoAccessControlPackages
import com.github.kr328.clash.settings.ui.sortAccessControlApps
import com.github.kr328.clash.settings.ui.toggleAccessControlSelectedPackage
import com.github.kr328.clash.settings.ui.updateAccessControlReverse
import com.github.kr328.clash.settings.ui.updateAccessControlSelectedPackages
import com.github.kr328.clash.settings.ui.updateAccessControlShowSystemApps
import com.github.kr328.clash.settings.ui.updateAccessControlSort
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
  AndroidViewModel(app), AccessControlActions, DefaultLifecycleObserver {
  private val appContext = app
  private val uiStore = UiStore(app)
  private val serviceStore = ServiceStore(app)
  private var reloadAppsJob: Job? = null

  val uiState: StateFlow<UiState>
    field =
      MutableStateFlow(
        UiState(
          apps = emptyList(),
          settings =
            AccessControlSettingsState(
              selected = emptySet(),
              sort = uiStore.accessControlSort,
              reverse = uiStore.accessControlReverse,
              showSystemApps = uiStore.accessControlSystemApp,
            ),
        )
      )

  val clashRunning: StateFlow<Boolean> = Remote.broadcasts.clashRunningFlow

  init {
    viewModelScope.launch {
      val selected = withContext(Dispatchers.IO) { serviceStore.accessControlPackages }
      uiState.update {
        it.copy(settings = updateAccessControlSelectedPackages(it.settings, selected))
      }
      reloadApps()
    }
  }

  override fun onStop(owner: LifecycleOwner) {
    // Intended to use non-viewModel scope as we need the action to be called on disposed.
    Global.launch {
      val selected = uiState.value.settings.selected
      val persistedSelection = serviceStore.accessControlPackages
      val changed = selected != persistedSelection

      if (changed) {
        serviceStore.accessControlPackages = selected
      }
      if (clashRunning.value && changed) {
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

  override fun toggleApp(packageName: String) {
    uiState.update { state ->
      state.copy(settings = toggleAccessControlSelectedPackage(state.settings, packageName))
    }
  }

  override fun selectAll() {
    viewModelScope.launch {
      val all =
        withContext(Dispatchers.Default) {
          val state = uiState.value
          selectAllAccessControlPackages(
            state = state.settings,
            packageNames = state.apps.map(AppInfo::packageName),
          )
        }
      uiState.update { it.copy(settings = all) }
    }
  }

  override fun selectNone() {
    uiState.update { it.copy(settings = selectNoAccessControlPackages(it.settings)) }
  }

  override fun selectInvert() {
    viewModelScope.launch {
      val selected =
        withContext(Dispatchers.Default) {
          val state = uiState.value
          invertAccessControlPackages(
            state = state.settings,
            packageNames = state.apps.map(AppInfo::packageName),
          )
        }
      uiState.update { it.copy(settings = selected) }
    }
  }

  override fun importFromClipboard() {
    val clipboard = appContext.getSystemService<ClipboardManager>()
    val data = clipboard?.primaryClip

    if (data != null && data.itemCount > 0) {
      val state = uiState.value
      val selected =
        importAccessControlPackages(
          state = state.settings,
          clipboardText = data.getItemAt(0).text?.toString(),
          installedPackageNames = state.apps.map(AppInfo::packageName),
        )
      uiState.update { it.copy(settings = selected) }
    }
  }

  override fun exportToClipboard() {
    val clipboard = appContext.getSystemService<ClipboardManager>()
    val data =
      ClipData.newPlainText(
        "packages",
        exportAccessControlPackages(uiState.value.settings.selected),
      )
    clipboard?.setPrimaryClip(data)
  }

  override fun updateSort(sort: AccessControlSort) {
    uiStore.accessControlSort = sort
    uiState.update { it.copy(settings = updateAccessControlSort(it.settings, sort)) }
    reloadApps()
  }

  override fun updateReverse(reverse: Boolean) {
    uiStore.accessControlReverse = reverse
    uiState.update { it.copy(settings = updateAccessControlReverse(it.settings, reverse)) }
    reloadApps()
  }

  override fun updateShowSystemApps(show: Boolean) {
    uiStore.accessControlSystemApp = show
    uiState.update { it.copy(settings = updateAccessControlShowSystemApps(it.settings, show)) }
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

      uiState.update { it.copy(apps = apps) }
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
      val apps =
        pm
          .getInstalledPackagesCompat(PackageManager.GET_PERMISSIONS)
          .asSequence()
          .filter { it.packageName != appContext.packageName }
          .filter { it.applicationInfo != null }
          .filter {
            it.requestedPermissions?.contains(Manifest.permission.INTERNET) == true ||
              it.applicationInfo!!.uid < Process.FIRST_APPLICATION_UID
          }
          .filter { showSystemApps || !it.isSystemApp }
          .map { it.toAppInfo(pm) }
          .toList()

      sortAccessControlApps(
        apps = apps,
        selectedPackageNames = selected,
        sort = sort,
        reverse = reverse,
        packageName = AppInfo::packageName,
        label = AppInfo::label,
        installTime = AppInfo::installTime,
        updateTime = AppInfo::updateDate,
      )
    }

  private val PackageInfo.isSystemApp: Boolean
    get() = applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) != 0

  data class UiState(
    val apps: List<AppInfo>,
    val settings: AccessControlSettingsState,
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
