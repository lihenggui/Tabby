package com.github.kr328.clash.app

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color.TRANSPARENT
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.annotation.StringRes
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.util.mainIntent
import com.github.kr328.clash.common.util.unsafeLazy
import com.github.kr328.clash.common.util.uuid
import com.github.kr328.clash.core.model.DarkMode
import com.github.kr328.clash.engine.android.AndroidProfileRepository
import com.github.kr328.clash.engine.api.ProfileRepository
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.store.UiStore
import com.github.kr328.clash.glue.util.startClashService
import com.github.kr328.clash.glue.util.stopClashService
import com.github.kr328.clash.ui.nav.rememberNavBackStackBuilder
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  private val uiStore by unsafeLazy { UiStore(this) }
  private val profileRepository: ProfileRepository by unsafeLazy { AndroidProfileRepository() }
  private val pendingExternalAppIntents = mutableListOf<Intent>()
  private var handleExternalAppIntent: ((Intent) -> Unit)? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (intent.handleExternalQuickAction()) {
      finish()
      return
    }
    when (tabbyInitialExternalAppQueueAction(savedStateRestored = savedInstanceState != null)) {
      TabbyInitialExternalAppQueueAction.Enqueue -> enqueueExternalAppIntent(intent)
      TabbyInitialExternalAppQueueAction.Ignore -> Unit
    }

    setContent {
      val backStack = rememberNavBackStackBuilder { addAll(tabbyInitialBackStack()) }
      val uiValueState by uiStore.valueState.collectAsStateWithLifecycle()
      val darkMode = uiValueState.darkMode
      val entryProvider = remember(backStack) { androidEntryProvider(backStack) }

      DisposableEffect(backStack) {
        val handler: (Intent) -> Unit = { intent -> intent.handleAction(backStack) }
        handleExternalAppIntent = handler
        pendingExternalAppIntents.forEach(handler)
        pendingExternalAppIntents.clear()

        onDispose {
          if (handleExternalAppIntent === handler) {
            handleExternalAppIntent = null
          }
        }
      }

      LaunchedEffect(darkMode) { edgeToEdge(darkMode) }

      TabbyApp(
        darkMode = darkMode,
        backStack = backStack,
        entryProvider = entryProvider,
        onBack = { backStack.removeLastOrNull() },
      )
    }

    requestNotificationPermission()
    setExcludeFromRecents()
    setupShortcuts()
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    if (intent.handleExternalQuickAction()) return
    enqueueExternalAppIntent(intent)
  }

  private fun Intent.handleAction(backStack: MutableList<NavKey>) {
    when (val plan = tabbyExternalAppActionPlan(tabbyExternalAppAction())) {
      TabbyExternalAppActionPlan.InstallProfile ->
        data?.let { handleInstallConfigUri(it, backStack) }
      is TabbyExternalAppActionPlan.OpenRoute ->
        backStack.handleTabbyExternalRouteAction(plan.routeAction)
      TabbyExternalAppActionPlan.Ignore -> Unit
    }
  }

  private fun enqueueExternalAppIntent(intent: Intent) {
    handleExternalAppIntent?.invoke(intent) ?: pendingExternalAppIntents.add(intent)
  }

  private fun Intent.handleExternalQuickAction(): Boolean {
    return when (
      val handlingPlan =
        tabbyExternalQuickActionHandlingPlan(
          action = tabbyExternalQuickAction(),
          clashRunning = Remote.broadcasts.clashRunning,
        )
    ) {
      is TabbyExternalQuickActionHandlingPlan.Handle -> {
        handleExternalQuickActionPlan(handlingPlan.actionPlan)
        true
      }
      TabbyExternalQuickActionHandlingPlan.Ignore -> false
    }
  }

  private fun handleExternalQuickActionPlan(plan: TabbyExternalQuickActionPlan) {
    when (plan) {
      TabbyExternalQuickActionPlan.StartClash -> startClash()
      TabbyExternalQuickActionPlan.StopClash -> stopClash()
      TabbyExternalQuickActionPlan.ShowAlreadyStarted,
      TabbyExternalQuickActionPlan.ShowAlreadyStopped -> plan.androidToastResource()?.let(::toast)
    }
  }

  private fun startClash() {
    toast(
      tabbyStartClashResultAction(vpnPermissionRequired = startClashService() != null)
        .androidToastResource()
    )
  }

  private fun stopClash() {
    stopClashService()
    toast(tabbyStopClashResultAction().androidToastResource())
  }

  private fun requestNotificationPermission() {
    when (
      tabbyNotificationPermissionActionFromPlatformState(
        sdkVersion = Build.VERSION.SDK_INT,
        runtimePermissionSdkVersion = Build.VERSION_CODES.TIRAMISU,
        permissionResult = ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS),
        grantedResult = PackageManager.PERMISSION_GRANTED,
      )
    ) {
      TabbyNotificationPermissionAction.RequestNotificationPermission ->
        registerForActivityResult(RequestPermission(), callback = {}).launch(POST_NOTIFICATIONS)
      TabbyNotificationPermissionAction.Ignore -> Unit
    }
  }

  private fun setExcludeFromRecents() {
    val action = tabbyRecentsTaskAction(hideFromRecents = uiStore.hideFromRecents)
    checkNotNull(getSystemService<ActivityManager>()).appTasks.forEach { task ->
      task.setExcludeFromRecents(action.excludeFromRecents)
    }
  }

  private fun edgeToEdge(darkMode: DarkMode) {
    val systemBars =
      tabbyEdgeToEdgeSystemBarMode(tabbyEdgeToEdgeStyle(darkMode)).androidSystemBarStyle()
    enableEdgeToEdge(statusBarStyle = systemBars, navigationBarStyle = systemBars)
    // TODO: https://issuetracker.google.com/issues/298296168
    //  Fix for three-button nav not properly going edge-to-edge.
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets -> insets }
  }

  private fun setupShortcuts() {
    val shortcutSpecs =
      tabbyExternalQuickActionShortcutPlan(appIconHidden = uiStore.hideAppIcon)
        .androidShortcutPlatformSpecs() ?: return
    val shortcuts = shortcutSpecs.map { shortcut ->
      ShortcutInfoCompat.Builder(this, shortcut.id)
        .setShortLabel(getString(shortcut.shortLabel))
        .setLongLabel(getString(shortcut.longLabel))
        .setIcon(IconCompat.createWithResource(this, shortcut.icon))
        .setIntent(mainIntent { action = shortcut.intentAction }.addFlags(shortcut.intentFlags))
        .setRank(shortcut.rank)
        .build()
    }

    ShortcutManagerCompat.setDynamicShortcuts(this, shortcuts)
  }

  private fun handleInstallConfigUri(uri: Uri, backStack: MutableList<NavKey>) {
    val request =
      tabbyInstallProfileRequest(
        source = uri.getQueryParameter("url"),
        type = uri.getQueryParameter("type"),
        name = uri.getQueryParameter("name"),
        defaultName = getString(CommonR.string.new_profile),
      ) ?: return
    lifecycleScope.launch {
      val uuid = tabbyInstallProfile(profileRepository, request)
      when (val action = tabbyInstallProfileResultAction(uuid)) {
        is TabbyInstallProfileResultAction.OpenRoute ->
          backStack.handleTabbyExternalRouteAction(action.routeAction)
      }
    }
  }
}

private fun Activity.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_LONG) {
  Toast.makeText(this, resId, duration).show()
}

private fun TabbyExternalQuickActionPlan.androidToastResource(): Int? =
  tabbyExternalQuickActionPlanFeedbackResource(
    plan = this,
    alreadyStartedResource = R.string.external_control_already_started,
    alreadyStoppedResource = R.string.external_control_already_stopped,
  )

private fun TabbyStartClashResultAction.androidToastResource(): Int =
  tabbyStartClashResultFeedbackResource(
    action = this,
    vpnPermissionRequiredResource = CommonR.string.unable_to_start_vpn,
    startedResource = R.string.external_control_started,
  )

private fun TabbyStopClashResultAction.androidToastResource(): Int =
  tabbyStopClashResultFeedbackResource(
    action = this,
    stoppedResource = R.string.external_control_stopped,
  )

private fun TabbyEdgeToEdgeSystemBarMode.androidSystemBarStyle(): SystemBarStyle {
  return forcedDarkMode?.let { forcedDarkMode ->
    SystemBarStyle.auto(TRANSPARENT, TRANSPARENT) { forcedDarkMode }
  } ?: SystemBarStyle.auto(TRANSPARENT, TRANSPARENT)
}

private fun Intent.tabbyExternalQuickAction(): TabbyExternalQuickAction? =
  tabbyExternalQuickActionFromString(
    action = action,
    toggleClashAction = Intents.ACTION_TOGGLE_CLASH,
    startClashAction = Intents.ACTION_START_CLASH,
    stopClashAction = Intents.ACTION_STOP_CLASH,
  )

private fun Intent.tabbyExternalAppAction(): TabbyExternalAppAction? =
  tabbyExternalAppActionFromString(
    action = action,
    requestAvailable = data != null,
    profilePropertiesUuid = uuid,
    installProfileAction = Intent.ACTION_VIEW,
    profilePropertiesAction = Intents.ACTION_PROPERTIES,
    logsAction = Intents.ACTION_LOGCAT,
    appCrashedAction = Intents.ACTION_APP_CRASHED,
    apkBrokenAction = Intents.ACTION_APK_BROKEN,
  )

private fun TabbyExternalQuickActionShortcutPlan.androidShortcutPlatformSpecs():
  List<TabbyExternalQuickActionShortcutPlatformSpec>? =
  tabbyExternalQuickActionShortcutPlatformSpecs(
    plan = this,
    toggleClashAction = Intents.ACTION_TOGGLE_CLASH,
    startClashAction = Intents.ACTION_START_CLASH,
    stopClashAction = Intents.ACTION_STOP_CLASH,
    openInNewTaskFlag = Intent.FLAG_ACTIVITY_NEW_TASK,
    excludeFromRecentsFlag = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS,
    noAnimationFlag = Intent.FLAG_ACTIVITY_NO_ANIMATION,
    toggleShortLabel = R.string.shortcut_toggle_short,
    toggleLongLabel = R.string.shortcut_toggle_long,
    toggleIcon = R.drawable.ic_toggle_all,
    startShortLabel = R.string.shortcut_start_short,
    startLongLabel = R.string.shortcut_start_long,
    startIcon = R.drawable.ic_toggle_on,
    stopShortLabel = R.string.shortcut_stop_short,
    stopLongLabel = R.string.shortcut_stop_long,
    stopIcon = R.drawable.ic_toggle_off,
  )
