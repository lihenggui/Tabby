package com.github.kr328.clash.profile.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.glue.util.elapsedIntervalString
import com.github.kr328.clash.glue.util.toDateStr
import com.github.kr328.clash.glue.util.toString
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
      Idle -> Unit
      OpenCreate -> onOpenCreate()
      is OpenEdit -> onOpenEdit(event.uuid)
      is ShowMessage -> {
        snackbarHostState.showSnackbar(message = event.message)
      }
      is ShowEditableMessage -> {
        val result =
          snackbarHostState.showSnackbar(
            message = event.message,
            actionLabel = editText,
            duration = SnackbarDuration.Long,
          )

        if (result == SnackbarResult.ActionPerformed) {
          onOpenEdit(event.uuid)
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
        formatType = { type -> type.toString(context) },
        formatUnsavedType = { typeText ->
          context.getString(R.string.format_type_unsaved, typeText)
        },
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
