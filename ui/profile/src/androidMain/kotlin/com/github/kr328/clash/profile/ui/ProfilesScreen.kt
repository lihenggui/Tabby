package com.github.kr328.clash.profile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.engine.android.AndroidProfileRepository
import com.github.kr328.clash.glue.remote.Broadcasts
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.util.toDateStr
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

  ProfileRepositoryProfilesListRouteContent(
    profileRepository = profileRepository,
    onCreate = onOpenCreate,
    onEdit = onOpenEdit,
    modifier = modifier,
    broadcastEvents = broadcastEvents,
    formatExpire = { expire -> expire.toDateStr() },
    formatElapsedMillis = { elapsed -> elapsedTimeTextString(context, elapsed) },
    onActionError = { cause -> Log.e("Profile action failed: ${cause.message}", cause) },
  )
}

private fun Broadcasts.Event.toProfilesEvent(): ProfilesBroadcastEvent =
  when (this) {
    Broadcasts.Event.ServiceRecreated ->
      profilesBroadcastEventFromSource(ProfilesBroadcastSourceEventKind.ServiceRecreated)
    Broadcasts.Event.Started ->
      profilesBroadcastEventFromSource(ProfilesBroadcastSourceEventKind.Started)
    is Broadcasts.Event.Stopped ->
      profilesBroadcastEventFromSource(ProfilesBroadcastSourceEventKind.Stopped)
    Broadcasts.Event.ProfileChanged ->
      profilesBroadcastEventFromSource(ProfilesBroadcastSourceEventKind.ProfileChanged)
    is Broadcasts.Event.ProfileUpdateCompleted ->
      profilesBroadcastEventFromSource(
        kind = ProfilesBroadcastSourceEventKind.ProfileUpdateCompleted,
        uuid = uuid,
      )
    is Broadcasts.Event.ProfileUpdateFailed ->
      profilesBroadcastEventFromSource(
        kind = ProfilesBroadcastSourceEventKind.ProfileUpdateFailed,
        uuid = uuid,
        reason = reason,
      )
    Broadcasts.Event.ProfileLoaded ->
      profilesBroadcastEventFromSource(ProfilesBroadcastSourceEventKind.ProfileLoaded)
  }
