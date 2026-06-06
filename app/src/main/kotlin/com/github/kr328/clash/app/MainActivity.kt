package com.github.kr328.clash.app

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
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
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.application
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  private val uiStore by unsafeLazy { UiStore(this) }
  private val viewModel: ViewModel by
    viewModels(factoryProducer = { ViewModel.Factory(this@MainActivity) })
  private inline val backStack
    get() = viewModel.backStack

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (intent.handleExternalQuickAction()) {
      finish()
      return
    }
    intent.handleAction(backStack)

    setContent {
      val uiValueState by uiStore.valueState.collectAsStateWithLifecycle()
      val darkMode = uiValueState.darkMode
      val entryProvider = remember(backStack) { androidEntryProvider(backStack) }

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
    intent.handleAction(backStack)
  }

  private fun Intent.handleAction(backStack: MutableList<NavKey>) {
    val appAction =
      when (action) {
        Intent.ACTION_VIEW -> TabbyExternalAppAction.InstallProfile(requestAvailable = data != null)
        Intents.ACTION_PROPERTIES -> TabbyExternalAppAction.OpenProfileProperties(uuid)
        Intents.ACTION_LOGCAT -> TabbyExternalAppAction.OpenLogs
        Intents.ACTION_APP_CRASHED -> TabbyExternalAppAction.OpenAppCrashed
        Intents.ACTION_APK_BROKEN -> TabbyExternalAppAction.OpenApkBroken
        else -> return
      }

    when (val plan = tabbyExternalAppActionPlan(appAction)) {
      TabbyExternalAppActionPlan.InstallProfile -> data?.let(viewModel::handleInstallConfigUri)
      is TabbyExternalAppActionPlan.OpenRoute ->
        backStack.handleTabbyExternalRouteAction(plan.routeAction)
      TabbyExternalAppActionPlan.Ignore -> Unit
    }
  }

  private fun Intent.handleExternalQuickAction(): Boolean {
    val quickAction =
      when (action) {
        Intents.ACTION_TOGGLE_CLASH -> TabbyExternalQuickAction.ToggleClash
        Intents.ACTION_START_CLASH -> TabbyExternalQuickAction.StartClash
        Intents.ACTION_STOP_CLASH -> TabbyExternalQuickAction.StopClash
        else -> return false
      }
    when (tabbyExternalQuickActionPlan(quickAction, Remote.broadcasts.clashRunning)) {
      TabbyExternalQuickActionPlan.StartClash -> startClash()
      TabbyExternalQuickActionPlan.StopClash -> stopClash()
      TabbyExternalQuickActionPlan.ShowAlreadyStarted ->
        toast(R.string.external_control_already_started)
      TabbyExternalQuickActionPlan.ShowAlreadyStopped ->
        toast(R.string.external_control_already_stopped)
    }
    return true
  }

  private fun startClash() {
    when (tabbyStartClashResultAction(vpnPermissionRequired = startClashService() != null)) {
      TabbyStartClashResultAction.ShowVpnPermissionRequired ->
        toast(CommonR.string.unable_to_start_vpn)
      TabbyStartClashResultAction.ShowStarted -> toast(R.string.external_control_started)
    }
  }

  private fun stopClash() {
    stopClashService()
    when (tabbyStopClashResultAction()) {
      TabbyStopClashResultAction.ShowStopped -> toast(R.string.external_control_stopped)
    }
  }

  private fun requestNotificationPermission() {
    when (
      tabbyNotificationPermissionAction(
        runtimePermissionRequired = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
        permissionGranted =
          ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED,
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
      when (tabbyEdgeToEdgeStyle(darkMode)) {
        TabbyEdgeToEdgeStyle.ForceDark -> SystemBarStyle.auto(TRANSPARENT, TRANSPARENT) { true }
        TabbyEdgeToEdgeStyle.ForceLight -> SystemBarStyle.auto(TRANSPARENT, TRANSPARENT) { false }
        TabbyEdgeToEdgeStyle.Auto -> SystemBarStyle.auto(TRANSPARENT, TRANSPARENT)
      }
    enableEdgeToEdge(statusBarStyle = systemBars, navigationBarStyle = systemBars)
    // TODO: https://issuetracker.google.com/issues/298296168
    //  Fix for three-button nav not properly going edge-to-edge.
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets -> insets }
  }

  private fun setupShortcuts() {
    val flags =
      Intent.FLAG_ACTIVITY_NEW_TASK or
        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
        Intent.FLAG_ACTIVITY_NO_ANIMATION

    val shortcuts =
      when (val plan = tabbyExternalQuickActionShortcutPlan(appIconHidden = uiStore.hideAppIcon)) {
        is TabbyExternalQuickActionShortcutPlan.Install ->
          plan.shortcuts.map { shortcut ->
            val resources = shortcut.action.shortcutResources()
            ShortcutInfoCompat.Builder(this, shortcut.id)
              .setShortLabel(getString(resources.shortLabel))
              .setLongLabel(getString(resources.longLabel))
              .setIcon(IconCompat.createWithResource(this, resources.icon))
              .setIntent(mainIntent { action = shortcut.action.intentAction() }.addFlags(flags))
              .setRank(shortcut.rank)
              .build()
          }
        TabbyExternalQuickActionShortcutPlan.Skip -> return
      }

    ShortcutManagerCompat.setDynamicShortcuts(this, shortcuts)
  }

  private class ViewModel(application: Application) : AndroidViewModel(application) {
    val backStack = tabbyInitialBackStack()
    private val profileRepository: ProfileRepository = AndroidProfileRepository()

    fun handleInstallConfigUri(uri: Uri) {
      val request =
        tabbyInstallProfileRequest(
          source = uri.getQueryParameter("url"),
          type = uri.getQueryParameter("type"),
          name = uri.getQueryParameter("name"),
          defaultName = application.getString(CommonR.string.new_profile),
        ) ?: return
      viewModelScope.launch {
        val uuid =
          profileRepository.create(request.type, request.name).also {
            profileRepository.patch(it, request.name, request.source, 0)
          }
        backStack.handleTabbyExternalRouteAction(
          TabbyExternalRouteAction.OpenProfileProperties(uuid)
        )
      }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
        ViewModel(application = context.applicationContext as Application) as T
    }
  }
}

private fun Activity.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_LONG) {
  Toast.makeText(this, resId, duration).show()
}

private data class AndroidShortcutResources(
  @StringRes val shortLabel: Int,
  @StringRes val longLabel: Int,
  val icon: Int,
)

private fun TabbyExternalQuickAction.intentAction(): String =
  when (this) {
    TabbyExternalQuickAction.ToggleClash -> Intents.ACTION_TOGGLE_CLASH
    TabbyExternalQuickAction.StartClash -> Intents.ACTION_START_CLASH
    TabbyExternalQuickAction.StopClash -> Intents.ACTION_STOP_CLASH
  }

private fun TabbyExternalQuickAction.shortcutResources(): AndroidShortcutResources =
  when (this) {
    TabbyExternalQuickAction.ToggleClash ->
      AndroidShortcutResources(
        shortLabel = R.string.shortcut_toggle_short,
        longLabel = R.string.shortcut_toggle_long,
        icon = R.drawable.ic_toggle_all,
      )
    TabbyExternalQuickAction.StartClash ->
      AndroidShortcutResources(
        shortLabel = R.string.shortcut_start_short,
        longLabel = R.string.shortcut_start_long,
        icon = R.drawable.ic_toggle_on,
      )
    TabbyExternalQuickAction.StopClash ->
      AndroidShortcutResources(
        shortLabel = R.string.shortcut_stop_short,
        longLabel = R.string.shortcut_stop_long,
        icon = R.drawable.ic_toggle_off,
      )
  }
