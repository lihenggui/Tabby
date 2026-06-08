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
import com.github.kr328.clash.core.model.AccessControlSort
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.store.UiStore
import com.github.kr328.clash.glue.util.startClashService
import com.github.kr328.clash.glue.util.stopClashService
import com.github.kr328.clash.service.store.ServiceStore
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
  val clashRunning by Remote.broadcasts.clashRunningFlow.collectAsStateWithLifecycle()
  var selected by remember { mutableStateOf(emptySet<String>()) }
  var sort by remember { mutableStateOf(uiStore.accessControlSort) }
  var reverse by remember { mutableStateOf(uiStore.accessControlReverse) }
  var showSystemApps by remember { mutableStateOf(uiStore.accessControlSystemApp) }
  var androidApps by remember { mutableStateOf(emptyList<AndroidAccessControlApp>()) }
  var reloadRequest by remember { mutableStateOf<AccessControlReloadRequest?>(null) }
  val currentSelected = rememberUpdatedState(selected)
  val currentClashRunning = rememberUpdatedState(clashRunning)
  val apps =
    remember(androidApps) { androidApps.map(AndroidAccessControlApp::toAccessControlPackage) }
  val icons = remember(androidApps) { androidApps.associate { it.packageName to it.icon } }

  LaunchedEffect(appContext, serviceStore) {
    val persistedSelected = withContext(Dispatchers.IO) { serviceStore.accessControlPackages }
    selected = persistedSelected
    reloadRequest =
      accessControlReloadRequest(
        selected = persistedSelected,
        sort = sort,
        reverse = reverse,
        showSystemApps = showSystemApps,
      )
  }

  LaunchedEffect(appContext, reloadRequest) {
    val request = reloadRequest ?: return@LaunchedEffect
    androidApps = appContext.loadAndroidAccessControlApps(request)
  }

  DisposableEffect(lifecycleOwner, appContext, serviceStore) {
    val observer = LifecycleEventObserver { _, event ->
      if (
        accessControlPersistRequestedFromStopEvent(isStopEvent = event == Lifecycle.Event.ON_STOP)
      ) {
        Global.launch {
          appContext.persistAccessControlSelection(
            serviceStore = serviceStore,
            selected = currentSelected.value,
            clashRunning = currentClashRunning.value,
          )
        }
      }
    }

    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  fun reloadApps(
    selectedSnapshot: Set<String> = currentSelected.value,
    sortSnapshot: AccessControlSort = sort,
    reverseSnapshot: Boolean = reverse,
    showSystemAppsSnapshot: Boolean = showSystemApps,
  ) {
    reloadRequest =
      accessControlReloadRequest(
        selected = selectedSnapshot,
        sort = sortSnapshot,
        reverse = reverseSnapshot,
        showSystemApps = showSystemAppsSnapshot,
      )
  }

  AccessControlRouteContent(
    modifier = modifier,
    initialApps = apps,
    initialSelected = selected,
    initialSort = sort,
    initialReverse = reverse,
    initialShowSystemApps = showSystemApps,
    onSelectedChange = { selected = it },
    onSortChange = {
      uiStore.accessControlSort = it
      sort = it
      reloadApps(sortSnapshot = it)
    },
    onReverseChange = {
      uiStore.accessControlReverse = it
      reverse = it
      reloadApps(reverseSnapshot = it)
    },
    onShowSystemAppsChange = {
      uiStore.accessControlSystemApp = it
      showSystemApps = it
      reloadApps(showSystemAppsSnapshot = it)
    },
    onImportClipboardPayload = appContext::accessControlClipboardImportPayload,
    onExportClipboardText = appContext::exportAccessControlClipboardText,
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
  return accessControlPackageFromPlatformPayload(
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
  clashRunning: Boolean,
) {
  val persistedSelection = serviceStore.accessControlPackages
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

private fun Context.exportAccessControlClipboardText(text: String) {
  val payload = accessControlExportPayload(text)
  val clipboard = getSystemService<ClipboardManager>()
  val data = ClipData.newPlainText(payload.label, payload.text)
  clipboard?.setPrimaryClip(data)
}

private val PackageInfo.isSystemApp: Boolean
  get() =
    accessControlSystemAppFromPlatformFlags(
      flags = applicationInfo?.flags,
      systemAppFlag = ApplicationInfo.FLAG_SYSTEM,
    )

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
