package com.github.kr328.clash.settings.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Process
import android.widget.ImageView
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.compat.getInstalledPackagesCompat
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.store.UiStore
import com.github.kr328.clash.glue.util.startClashService
import com.github.kr328.clash.glue.util.stopClashService
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.settingsstore.TabbyAccessControlSettings
import com.github.kr328.clash.settingsstore.TabbyAccessControlSettingsRepository
import com.github.kr328.clash.ui.theme.tabbyDimens
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

@Composable
internal fun AccessControlScreen(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val appContext = context.applicationContext
  val lifecycleOwner = LocalLifecycleOwner.current
  val uiStore = remember(appContext) { UiStore(appContext) }
  val serviceStore = remember(appContext) { ServiceStore(appContext) }
  val accessControlSettingsRepository =
    remember(uiStore, serviceStore) {
      TabbyAccessControlSettingsRepository(
        uiStoreProvider = uiStore.storeProvider,
        serviceStoreProvider = serviceStore.storeProvider,
      )
    }
  val accessControlSettingsDefaults =
    remember(uiStore, serviceStore) {
      TabbyAccessControlSettings(
        selectedPackages = serviceStore.accessControlPackages,
        sort = uiStore.accessControlSort,
        reverse = uiStore.accessControlReverse,
        showSystemApps = uiStore.accessControlSystemApp,
      )
    }
  val accessControlSettings =
    remember(accessControlSettingsRepository, accessControlSettingsDefaults) {
      accessControlSettingsRepository.query(accessControlSettingsDefaults)
    }
  val clashRunning by Remote.broadcasts.clashRunningFlow.collectAsStateWithLifecycle()
  var settingsState by remember {
    mutableStateOf(
      AccessControlSettingsState(
        selected = accessControlSettings.selectedPackages,
        sort = accessControlSettings.sort,
        reverse = accessControlSettings.reverse,
        showSystemApps = accessControlSettings.showSystemApps,
      )
    )
  }
  var androidApps by remember { mutableStateOf(emptyList<AndroidAccessControlApp>()) }
  var lastAppliedSelected by remember { mutableStateOf(accessControlSettings.selectedPackages) }
  var reloadRequest by remember {
    mutableStateOf(accessControlReloadRequest(settingsState))
  }
  val currentSelected = rememberUpdatedState(settingsState.selected)
  val currentAppliedSelected = rememberUpdatedState(lastAppliedSelected)
  val currentClashRunning = rememberUpdatedState(clashRunning)
  val apps =
    remember(androidApps) { androidApps.map(AndroidAccessControlApp::toAccessControlPackage) }
  val icons = remember(androidApps) { androidApps.associate { it.packageName to it.icon } }

  LaunchedEffect(appContext, reloadRequest) {
    androidApps = appContext.loadAndroidAccessControlApps(reloadRequest)
  }

  DisposableEffect(lifecycleOwner, appContext, serviceStore) {
    val observer = LifecycleEventObserver { _, event ->
      if (
        accessControlPersistRequestedFromStopEvent(isStopEvent = event == Lifecycle.Event.ON_STOP)
      ) {
        val selectedSnapshot = currentSelected.value
        val appliedSelectedSnapshot = currentAppliedSelected.value
        lastAppliedSelected = selectedSnapshot
        Global.launch {
          appContext.persistAccessControlSelection(
            serviceStore = serviceStore,
            selected = selectedSnapshot,
            persistedSelection = appliedSelectedSnapshot,
            clashRunning = currentClashRunning.value,
          )
        }
      }
    }

    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  fun reloadApps(settingsSnapshot: AccessControlSettingsState = settingsState) {
    reloadRequest = accessControlReloadRequest(settingsSnapshot)
  }

  AccessControlSettingsRepositoryRouteContent(
    repository = accessControlSettingsRepository,
    modifier = modifier,
    initialApps = apps,
    defaults = accessControlSettings,
    onSelectedPackagesChange = {
      settingsState = updateAccessControlSelectedPackages(settingsState, it)
    },
    onSortChange = {
      val updatedSettings = updateAccessControlSort(settingsState, it)
      settingsState = updatedSettings
      reloadApps(updatedSettings)
    },
    onReverseChange = {
      val updatedSettings = updateAccessControlReverse(settingsState, it)
      settingsState = updatedSettings
      reloadApps(updatedSettings)
    },
    onShowSystemAppsChange = {
      val updatedSettings = updateAccessControlShowSystemApps(settingsState, it)
      settingsState = updatedSettings
      reloadApps(updatedSettings)
    },
    onImportClipboardPayload = appContext::accessControlClipboardImportPayload,
    onExportClipboardPayload = appContext::exportAccessControlClipboardPayload,
    appIcon = { app -> icons[app.packageName]?.let { icon -> AccessControlAppIcon(icon) } },
  )
}

@Composable
private fun AccessControlAppIcon(icon: Drawable) {
  val dimens = tabbyDimens

  AndroidView(
    factory = { context ->
      ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
    },
    update = { it.setImageDrawable(icon) },
    modifier = Modifier.size(dimens.itemHeaderComponentSize),
  )
}

private data class AndroidAccessControlApp(
  val packageName: String,
  val label: String,
  val icon: Drawable,
  val installTime: Long,
  val updateDate: Long,
)

private fun AndroidAccessControlApp.toAccessControlPackage(): AccessControlPackage {
  return AccessControlPackage(
    packageName = packageName,
    label = label,
    installTime = installTime,
    updateDate = updateDate,
  )
}

private suspend fun Context.loadAndroidAccessControlApps(
  request: AccessControlReloadRequest
): List<AndroidAccessControlApp> =
  withContext(Dispatchers.IO) {
    val pm = packageManager
    loadAccessControlApps(
      packages = pm.getInstalledPackagesCompat(PackageManager.GET_PERMISSIONS),
      selectedPackageNames = request.selected,
      sort = request.sort,
      reverse = request.reverse,
      currentPackageName = packageName,
      showSystemApps = request.showSystemApps,
      packageName = PackageInfo::packageName,
      hasAppMetadata = { it.applicationInfo != null },
      hasInternetPermission = {
        it.requestedPermissions?.contains(Manifest.permission.INTERNET) == true
      },
      hasSystemUid = {
        it.applicationInfo?.uid?.let { uid -> uid < Process.FIRST_APPLICATION_UID } == true
      },
      isSystemApp = { it.isSystemApp },
      toApp = { it.toAccessControlApp(pm) },
      appPackageName = AndroidAccessControlApp::packageName,
      appLabel = AndroidAccessControlApp::label,
      appInstallTime = AndroidAccessControlApp::installTime,
      appUpdateTime = AndroidAccessControlApp::updateDate,
    )
  }

private suspend fun Context.persistAccessControlSelection(
  serviceStore: ServiceStore,
  selected: Set<String>,
  persistedSelection: Set<String>,
  clashRunning: Boolean,
) {
  val persistPlan =
    planAccessControlPersist(
      selected = selected,
      persistedSelection = persistedSelection,
      clashRunning = clashRunning,
    )

  if (persistPlan.shouldPersistSelection) {
    serviceStore.accessControlPackages = selected
  }
  if (persistPlan.shouldRestartService) {
    stopClashService()

    val stopped =
      withTimeoutOrNull(10.seconds) {
        Remote.broadcasts.clashRunningFlow.first { !it }
        true
      } ?: false

    if (!stopped) {
      // The stop signal may be lost; continue with a best-effort restart path.
      stopClashService()
    }

    startClashService()
  }
}

private fun Context.accessControlClipboardImportPayload(): AccessControlClipboardImportPayload {
  val clipboard = getSystemService<ClipboardManager>()
  val data = clipboard?.primaryClip
  val hasPrimaryClipItem = accessControlClipboardHasPrimaryClipItem(data?.itemCount)
  val clipboardText = if (hasPrimaryClipItem) data?.getItemAt(0)?.text?.toString() else null

  return accessControlClipboardImportPayload(
    hasPrimaryClipItem = hasPrimaryClipItem,
    clipboardText = clipboardText,
  )
}

private fun Context.exportAccessControlClipboardPayload(payload: AccessControlExportPayload) {
  val clipboard = getSystemService<ClipboardManager>()
  val data = ClipData.newPlainText(payload.label, payload.text)
  clipboard?.setPrimaryClip(data)
}

private val PackageInfo.isSystemApp: Boolean
  get() = applicationInfo?.flags?.let { it and ApplicationInfo.FLAG_SYSTEM != 0 } == true

private fun PackageInfo.toAccessControlApp(pm: PackageManager): AndroidAccessControlApp {
  val applicationInfo = checkNotNull(applicationInfo)
  return AndroidAccessControlApp(
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
