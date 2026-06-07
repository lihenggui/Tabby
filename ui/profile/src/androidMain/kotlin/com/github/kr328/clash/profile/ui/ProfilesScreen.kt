package com.github.kr328.clash.profile.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.glue.util.toDateStr
import com.github.kr328.clash.profile.R
import com.github.kr328.clash.profile.vm.ProfilesViewModel
import com.github.kr328.clash.ui.lifecycle.viewModelWithLifecycle
import kotlin.uuid.Uuid

@Composable
internal fun ProfilesScreen(
  modifier: Modifier = Modifier,
  viewModel: ProfilesViewModel = viewModelWithLifecycle(),
  onOpenCreate: () -> Unit,
  onOpenEdit: (Uuid) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val eventState by viewModel.eventState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }
  val editText = stringResource(R.string.edit)

  LaunchedEffect(eventState) {
    when (val action = profilesEventPlatformAction(eventState)) {
      ProfilesEventPlatformAction.Ignore -> Unit
      ProfilesEventPlatformAction.OpenCreate -> onOpenCreate()
      is ProfilesEventPlatformAction.OpenEdit -> onOpenEdit(action.uuid)
      is ProfilesEventPlatformAction.ShowMessage -> {
        snackbarHostState.showSnackbar(message = action.message)
      }
      is ProfilesEventPlatformAction.ShowEditableMessage -> {
        val result =
          snackbarHostState.showSnackbar(
            message = action.message,
            actionLabel = editText,
            duration = SnackbarDuration.Long,
          )

        when (profilesEditableSnackbarAction(result.toProfileSnackbarActionResult())) {
          ProfilesEditableSnackbarAction.OpenEdit -> onOpenEdit(action.uuid)
          ProfilesEditableSnackbarAction.Ignore -> Unit
        }
      }
    }
    viewModel.consumeEvent()
  }

  ProfilesListRouteContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    profiles = uiState.profiles,
    allUpdating = uiState.allUpdating,
    currentTimeMillis = uiState.currentTime,
    formatBytes = ::profileBinaryBytesText,
    formatExpire = { expire -> expire.toDateStr() },
    formatElapsedMillis = { elapsed -> elapsedTimeTextString(context, elapsed) },
    onUpdateAll = viewModel::onUpdateAll,
    onCreate = viewModel::onOpenCreate,
    onActivate = viewModel::onActivate,
    onUpdate = viewModel::onUpdate,
    onEdit = viewModel::onEdit,
    onDuplicate = viewModel::onDuplicate,
    onDelete = viewModel::onDelete,
  )
}
