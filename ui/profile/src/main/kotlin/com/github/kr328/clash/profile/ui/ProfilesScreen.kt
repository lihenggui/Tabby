package com.github.kr328.clash.profile.ui

import android.content.Context
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
import com.github.kr328.clash.core.model.Profile
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
    profiles = uiState.profiles.map { profile -> profile.toListItem(context, uiState.currentTime) },
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

private fun Profile.toListItem(context: Context, currentTime: Long): ProfileListItem {
  val profileTypeText =
    if (pending) {
      context.getString(R.string.format_type_unsaved, type.toString(context))
    } else {
      type.toString(context)
    }
  val showTraffic = download >= 2 && total > 1
  val usageText =
    if (showTraffic) {
      "${(download + upload).binaryBytes} / ${total.binaryBytes}"
    } else {
      null
    }
  val progress =
    if (showTraffic) {
      ((download + upload).toDouble() / total.toDouble() * 1000).toInt().coerceIn(0, 1000)
    } else {
      0
    }

  return ProfileListItem(
    profile = this,
    typeText = profileTypeText,
    usageText = usageText,
    expireText = expire.takeIf { it != 0L }?.toDateStr(),
    updatedAtText = (currentTime - updatedAt).elapsedIntervalString(context),
    trafficProgress = progress,
  )
}
