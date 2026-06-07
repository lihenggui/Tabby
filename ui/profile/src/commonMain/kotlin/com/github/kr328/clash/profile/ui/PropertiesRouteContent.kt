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
import com.github.kr328.clash.core.model.Profile
import kotlin.uuid.Uuid
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tabby.ui.profile.generated.resources.Res as ProfileRes
import tabby.ui.profile.generated.resources.empty_name
import tabby.ui.profile.generated.resources.invalid_url

@Composable
fun PropertiesRouteContent(
  modifier: Modifier = Modifier,
  profile: Profile = defaultPropertiesRouteProfile(),
  tipsProperties: AnnotatedString = AnnotatedString("Accept Only Tabby Config"),
  onBrowseFiles: (Profile) -> Unit = {},
  onCommit: (Profile) -> Unit = {},
  onProfileChange: (Profile) -> Unit = {},
  onFinish: (success: Boolean) -> Unit = {},
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()
  val emptyNameMessage = stringResource(ProfileRes.string.empty_name)
  val invalidUrlMessage = stringResource(ProfileRes.string.invalid_url)
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

  PropertiesContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    profile = checkNotNull(uiState.profile),
    processing = uiState.processing,
    progressState = uiState.progress,
    showExitWithoutSavingDialog = showExitWithoutSavingDialog,
    tipsProperties = tipsProperties,
    onBack = onBack,
    onDismissExitWithoutSavingDialog = { showExitWithoutSavingDialog = false },
    onBrowseFiles = { uiState.profile?.let(onBrowseFiles) },
    onCommit = {
      when (val action = propertiesCommitAction(uiState)) {
        is PropertiesCommitAction.Commit -> {
          onCommit(action.profile)
          uiState = uiState.withSavedProfile(action.profile)
          onFinish(true)
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
