package com.github.kr328.clash.profile.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.github.kr328.clash.core.model.FetchStatus
import com.github.kr328.clash.core.model.Profile
import kotlin.uuid.Uuid
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import tabby.ui.profile.generated.resources.Res as ProfileRes
import tabby.ui.profile.generated.resources.empty_name
import tabby.ui.profile.generated.resources.format_fetching_configuration
import tabby.ui.profile.generated.resources.format_fetching_provider
import tabby.ui.profile.generated.resources.initializing
import tabby.ui.profile.generated.resources.invalid_url
import tabby.ui.profile.generated.resources.verifying
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.unavailable

@Composable
fun PropertiesRouteContent(
  modifier: Modifier = Modifier,
  profile: Profile = defaultPropertiesRouteProfile(),
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  tipsProperties: AnnotatedString = AnnotatedString("Accept Only Tabby Config"),
  onBrowseFiles: (Profile) -> Unit = {},
  onCommit: suspend (Profile, suspend (FetchStatus) -> Unit) -> Unit = { _, _ -> },
  onProfileChange: (Profile) -> Unit = {},
  onFinish: (success: Boolean) -> Unit = {},
) {
  val coroutineScope = rememberCoroutineScope()
  val emptyNameMessage = stringResource(ProfileRes.string.empty_name)
  val initializingMessage = stringResource(ProfileRes.string.initializing)
  val invalidUrlMessage = stringResource(ProfileRes.string.invalid_url)
  val unavailableMessage = stringResource(SharedRes.string.unavailable)
  val navigationEventState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
  var uiState by
    remember(profile) { mutableStateOf(propertiesInitialUiState().withLoadedProfile(profile)) }
  var showExitWithoutSavingDialog by
    rememberSaveable(profile.uuid.toString()) {
      mutableStateOf(false)
    }

  fun updateProfile(nextState: PropertiesUiState) {
    uiState = nextState
    nextState.profile?.let(onProfileChange)
  }

  fun requestClose() {
    onFinish(false)
  }

  suspend fun updateFetchStatus(status: FetchStatus) {
    uiState = uiState.withLocalizedFetchStatusProgress(status)
  }

  val onBack = {
    when (
      propertiesBackAction(
        processing = uiState.processing,
        showExitWithoutSavingDialog = showExitWithoutSavingDialog,
        hasUnsavedChanges = uiState.hasUnsavedChanges,
      )
    ) {
      PropertiesBackAction.Ignore -> Unit
      PropertiesBackAction.HideExitWithoutSavingDialog -> showExitWithoutSavingDialog = false
      PropertiesBackAction.ShowExitWithoutSavingDialog -> showExitWithoutSavingDialog = true
      PropertiesBackAction.RequestClose -> requestClose()
    }
  }

  NavigationBackHandler(
    state = navigationEventState,
    isBackEnabled = true,
    onBackCompleted = onBack,
  )

  PropertiesStateRouteContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    state = uiState,
    showExitWithoutSavingDialog = showExitWithoutSavingDialog,
    tipsProperties = tipsProperties,
    onBack = onBack,
    onDismissExitWithoutSavingDialog = { showExitWithoutSavingDialog = false },
    onBrowseFiles = { uiState.profile?.let(onBrowseFiles) },
    onCommit = {
      when (val action = propertiesCommitAction(uiState)) {
        is PropertiesCommitAction.Commit ->
          coroutineScope.launch {
            uiState = uiState.withProcessingStarted(initializingMessage)
            try {
              onCommit(action.profile, ::updateFetchStatus)
              uiState = uiState.withSavedProfile(action.profile)
              onFinish(true)
            } catch (e: CancellationException) {
              throw e
            } catch (e: Exception) {
              uiState = uiState.withProcessingFinished()
              snackbarHostState.showSnackbar(e.message ?: unavailableMessage)
            } finally {
              if (uiState.processing) {
                uiState = uiState.withProcessingFinished()
              }
            }
          }
        PropertiesCommitAction.ShowEmptyName ->
          coroutineScope.launch { snackbarHostState.showSnackbar(emptyNameMessage) }
        PropertiesCommitAction.ShowEmptySource ->
          coroutineScope.launch { snackbarHostState.showSnackbar(invalidUrlMessage) }
        PropertiesCommitAction.Ignore -> Unit
      }
    },
    onRequestClose = ::requestClose,
    onNameChanged = { name -> updateProfile(uiState.withProfileName(name)) },
    onUrlChanged = { url -> updateProfile(uiState.withProfileSource(url)) },
    onIntervalChanged = { interval -> updateProfile(uiState.withProfileInterval(interval)) },
  )
}

private suspend fun PropertiesUiState.withLocalizedFetchStatusProgress(
  status: FetchStatus
): PropertiesUiState {
  val nextProgress =
    when (status.action) {
      FetchStatus.Action.FetchConfiguration ->
        progress.withFetchConfigurationProgress(
          text =
            getString(
              ProfileRes.string.format_fetching_configuration,
              status.args.getOrNull(0).orEmpty(),
            )
        )
      FetchStatus.Action.FetchProviders ->
        progress.withFetchProvidersProgress(
          text =
            getString(
              ProfileRes.string.format_fetching_provider,
              status.args.getOrNull(0).orEmpty(),
            ),
          max = status.max,
          progress = status.progress,
        )
      FetchStatus.Action.Verifying ->
        progress.withVerifyingProgress(
          text = getString(ProfileRes.string.verifying),
          max = status.max,
          progress = status.progress,
        )
    }

  return withProgress(nextProgress)
}

@Composable
internal fun PropertiesStateRouteContent(
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState,
  state: PropertiesUiState,
  showExitWithoutSavingDialog: Boolean,
  tipsProperties: AnnotatedString,
  onBack: () -> Unit,
  onDismissExitWithoutSavingDialog: () -> Unit,
  onBrowseFiles: () -> Unit,
  onCommit: () -> Unit,
  onRequestClose: () -> Unit,
  onNameChanged: (String) -> Unit,
  onUrlChanged: (String) -> Unit,
  onIntervalChanged: (Long) -> Unit,
) {
  state.profile?.let { profile ->
    PropertiesContent(
      modifier = modifier,
      snackbarHostState = snackbarHostState,
      profile = profile,
      processing = state.processing,
      progressState = state.progress,
      showExitWithoutSavingDialog = showExitWithoutSavingDialog,
      tipsProperties = tipsProperties,
      onBack = onBack,
      onDismissExitWithoutSavingDialog = onDismissExitWithoutSavingDialog,
      onBrowseFiles = onBrowseFiles,
      onCommit = onCommit,
      onRequestClose = onRequestClose,
      onNameChanged = onNameChanged,
      onUrlChanged = onUrlChanged,
      onIntervalChanged = onIntervalChanged,
    )
  }
}

private fun defaultPropertiesRouteProfile(): Profile {
  return Profile(
    uuid = Uuid.fromLongs(0, 0),
    name = "Meta Profile",
    type = Profile.Type.Url,
    source = "https://example.com/config.yaml",
    active = false,
    interval = 0,
    upload = 0,
    download = 0,
    total = 0,
    expire = 0,
    updatedAt = 0,
    imported = false,
    pending = false,
  )
}
