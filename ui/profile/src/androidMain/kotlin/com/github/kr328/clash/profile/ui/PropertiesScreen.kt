package com.github.kr328.clash.profile.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.profile.R
import com.github.kr328.clash.profile.ui.PropertiesBackAction.HideExitWithoutSavingDialog
import com.github.kr328.clash.profile.ui.PropertiesBackAction.Ignore
import com.github.kr328.clash.profile.ui.PropertiesBackAction.RequestClose
import com.github.kr328.clash.profile.ui.PropertiesBackAction.ShowExitWithoutSavingDialog
import com.github.kr328.clash.profile.vm.PropertiesViewModel
import com.github.kr328.clash.ui.lifecycle.viewModelWithLifecycle
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

@Composable
internal fun PropertiesScreen(
  uuid: Uuid,
  modifier: Modifier = Modifier,
  viewModel: PropertiesViewModel = viewModelWithLifecycle(),
  onBrowseFiles: (Uuid) -> Unit,
  onFinish: (Boolean) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val eventState by viewModel.eventState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(uuid) { viewModel.init(uuid = uuid) }

  LaunchedEffect(eventState) {
    when (val event = eventState) {
      PropertiesEventState.Idle -> Unit
      is PropertiesEventState.BrowseFiles -> {
        onBrowseFiles(event.uuid)
      }
      is PropertiesEventState.Finish -> {
        onFinish(event.success)
      }
      is PropertiesEventState.ShowMessage -> {
        snackbarHostState.showSnackbar(message = event.message)
      }
    }
    viewModel.consumeEvent()
  }

  val profile = uiState.profile
  if (profile != null) {
    var showExitWithoutSavingDialog by rememberSaveable { mutableStateOf(false) }
    val onBack = {
      when (
        propertiesBackAction(
          processing = uiState.processing,
          showExitWithoutSavingDialog = showExitWithoutSavingDialog,
          hasUnsavedChanges = uiState.hasUnsavedChanges,
        )
      ) {
        Ignore -> Unit
        HideExitWithoutSavingDialog -> showExitWithoutSavingDialog = false
        ShowExitWithoutSavingDialog -> showExitWithoutSavingDialog = true
        RequestClose -> viewModel.onRequestClose()
      }
    }

    BackHandler(onBack = onBack)

    PropertiesContent(
      modifier = modifier,
      snackbarHostState = snackbarHostState,
      profile = profile,
      processing = uiState.processing,
      progressState = uiState.progress,
      showExitWithoutSavingDialog = showExitWithoutSavingDialog,
      tipsProperties = AnnotatedString.fromHtml(stringResource(R.string.tips_properties)),
      onBack = onBack,
      onDismissExitWithoutSavingDialog = { showExitWithoutSavingDialog = false },
      onBrowseFiles = viewModel::onBrowseFiles,
      onCommit = viewModel::onCommit,
      onRequestClose = viewModel::onRequestClose,
      onNameChanged = viewModel::onNameChanged,
      onUrlChanged = viewModel::onUrlChanged,
      onIntervalChanged = viewModel::onIntervalChanged,
    )
  }
}

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun PropertiesContentPreview() {
  PropertiesContent(
    snackbarHostState = SnackbarHostState(),
    profile =
      Profile(
        uuid = Uuid.fromLongs(0, 0),
        name = "Meta Profile",
        type = Profile.Type.Url,
        source = "https://example.com/config.yaml",
        active = false,
        interval = 60.minutes.inWholeMilliseconds,
        upload = 0,
        download = 0,
        total = 0,
        expire = 0,
        updatedAt = 0,
        imported = false,
        pending = false,
      ),
    processing = false,
    progressState = PropertiesProgressState(),
    showExitWithoutSavingDialog = false,
    tipsProperties = AnnotatedString("Accept Only Tabby Config"),
    onBack = {},
    onDismissExitWithoutSavingDialog = {},
    onBrowseFiles = {},
    onCommit = {},
    onRequestClose = {},
    onNameChanged = {},
    onUrlChanged = {},
    onIntervalChanged = {},
  )
}
