package com.github.kr328.clash.profile.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource as androidStringResource
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.engine.android.AndroidProfileRepository
import com.github.kr328.clash.glue.remote.Broadcasts
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.util.toDateStr
import com.github.kr328.clash.profile.R
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.map

@Composable
internal fun ProfilesScreen(
  modifier: Modifier = Modifier,
  onOpenCreate: () -> Unit,
  onOpenEdit: (Uuid) -> Unit,
) {
  val context = LocalContext.current
  val profileRepository = remember { AndroidProfileRepository() }
  val broadcastEvents = remember {
    Remote.broadcasts.event.map { event -> event.toProfilesEvent() }
  }
  val snackbarHostState = remember { SnackbarHostState() }
  val activeUnsavedTipsText = androidStringResource(R.string.active_unsaved_tips)
  val editText = androidStringResource(R.string.edit)
  val unknownText = androidStringResource(CommonR.string.unknown)

  ProfileRepositoryProfilesListRouteContent(
    profileRepository = profileRepository,
    onCreate = onOpenCreate,
    onEdit = onOpenEdit,
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    broadcastEvents = broadcastEvents,
    formatExpire = { expire -> expire.toDateStr() },
    formatElapsedMillis = { elapsed -> elapsedTimeTextString(context, elapsed) },
    onPendingActivation = { profile ->
      val result =
        snackbarHostState.showSnackbar(
          message = activeUnsavedTipsText,
          actionLabel = editText,
          duration = SnackbarDuration.Long,
        )

      when (profilesEditableSnackbarAction(result.toProfileSnackbarActionResult())) {
        ProfilesEditableSnackbarAction.OpenEdit -> onOpenEdit(profile.uuid)
        ProfilesEditableSnackbarAction.Ignore -> Unit
      }
    },
    onProfileUpdateCompleted = { _, profileName ->
      snackbarHostState.showSnackbar(
        message = context.getString(R.string.toast_profile_updated_complete, profileName)
      )
    },
    onProfileUpdateFailed = { uuid, profileName, reason ->
      val displayReason = profileUpdateFailureReasonText(reason, unknownText)
      val result =
        snackbarHostState.showSnackbar(
          message =
            context.getString(R.string.toast_profile_updated_failed, profileName, displayReason),
          actionLabel = editText,
          duration = SnackbarDuration.Long,
        )

      when (profilesEditableSnackbarAction(result.toProfileSnackbarActionResult())) {
        ProfilesEditableSnackbarAction.OpenEdit -> onOpenEdit(uuid)
        ProfilesEditableSnackbarAction.Ignore -> Unit
      }
    },
    onActionError = { cause -> Log.e("Profile action failed: ${cause.message}", cause) },
  )
}

private fun Broadcasts.Event.toProfilesEvent(): ProfilesBroadcastEvent =
  when (this) {
    Broadcasts.Event.ServiceRecreated ->
      profilesBroadcastEventFromPlatformPayload(ProfilesBroadcastEventKind.ServiceRecreated)
    Broadcasts.Event.Started ->
      profilesBroadcastEventFromPlatformPayload(ProfilesBroadcastEventKind.Started)
    is Broadcasts.Event.Stopped ->
      profilesBroadcastEventFromPlatformPayload(ProfilesBroadcastEventKind.Stopped)
    Broadcasts.Event.ProfileChanged ->
      profilesBroadcastEventFromPlatformPayload(ProfilesBroadcastEventKind.ProfileChanged)
    is Broadcasts.Event.ProfileUpdateCompleted ->
      profilesBroadcastEventFromPlatformPayload(
        kind = ProfilesBroadcastEventKind.ProfileUpdateCompleted,
        uuid = uuid,
      )
    is Broadcasts.Event.ProfileUpdateFailed ->
      profilesBroadcastEventFromPlatformPayload(
        kind = ProfilesBroadcastEventKind.ProfileUpdateFailed,
        uuid = uuid,
        reason = reason,
      )
    Broadcasts.Event.ProfileLoaded ->
      profilesBroadcastEventFromPlatformPayload(ProfilesBroadcastEventKind.ProfileLoaded)
  }
