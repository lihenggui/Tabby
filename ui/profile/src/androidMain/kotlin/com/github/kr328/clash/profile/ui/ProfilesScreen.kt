package com.github.kr328.clash.profile.ui

import android.content.Context
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
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.glue.util.elapsedIntervalString
import com.github.kr328.clash.glue.util.toDateStr
import com.github.kr328.clash.profile.R
import com.github.kr328.clash.profile.vm.ProfilesViewModel
import com.github.kr328.clash.ui.lifecycle.viewModelWithLifecycle
import kotlin.uuid.Uuid
import me.saket.bytesize.binaryBytes

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
    when (val event = eventState) {
      ProfilesEventState.Idle -> Unit
      ProfilesEventState.OpenCreate -> onOpenCreate()
      is ProfilesEventState.OpenEdit -> onOpenEdit(event.uuid)
      is ProfilesEventState.ShowMessage -> {
        snackbarHostState.showSnackbar(message = event.message)
      }
      is ProfilesEventState.ShowEditableMessage -> {
        val result =
          snackbarHostState.showSnackbar(
            message = event.message,
            actionLabel = editText,
            duration = SnackbarDuration.Long,
          )

        when (profilesEditableSnackbarAction(result.toProfileSnackbarActionResult())) {
          ProfilesEditableSnackbarAction.OpenEdit -> onOpenEdit(event.uuid)
          ProfilesEditableSnackbarAction.Ignore -> Unit
        }
      }
    }
    viewModel.consumeEvent()
  }

  ProfilesContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    profiles =
      uiState.toProfileListItems(
        formatTypeText = { typeText -> typeText.androidString(context) },
        formatBytes = { bytes -> bytes.binaryBytes.toString() },
        formatExpire = { expire -> expire.toDateStr() },
        formatElapsedMillis = { elapsed -> elapsed.elapsedIntervalString(context) },
      ),
    allUpdating = uiState.allUpdating,
    hasUpdatableProfile = uiState.hasUpdatableProfile,
    onUpdateAll = viewModel::onUpdateAll,
    onCreate = viewModel::onOpenCreate,
    onActivate = viewModel::onActivate,
    onUpdate = viewModel::onUpdate,
    onEdit = viewModel::onEdit,
    onDuplicate = viewModel::onDuplicate,
    onDelete = viewModel::onDelete,
  )
}

private fun ProfileTypeText.androidString(context: Context): String {
  val typeText = token.androidString(context)

  return if (pending) {
    context.getString(R.string.format_type_unsaved, typeText)
  } else {
    typeText
  }
}

private fun ProfileTypeTextToken.androidString(context: Context): String {
  return when (this) {
    ProfileTypeTextToken.File -> context.getString(CommonR.string.file)
    ProfileTypeTextToken.Url -> context.getString(CommonR.string.url)
    ProfileTypeTextToken.External -> context.getString(CommonR.string.external)
  }
}
