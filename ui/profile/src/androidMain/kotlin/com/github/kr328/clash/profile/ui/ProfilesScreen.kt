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
  profilesBroadcastEventFromPlatformPayload(
    event = this,
    kind = Broadcasts.Event::profilesBroadcastSourceEventKind,
    uuid = Broadcasts.Event::profilesUpdateUuid,
    reason = Broadcasts.Event::profilesUpdateReason,
  )

private fun Broadcasts.Event.profilesBroadcastSourceEventKind(): ProfilesBroadcastSourceEventKind =
  when (this) {
    Broadcasts.Event.ServiceRecreated -> ProfilesBroadcastSourceEventKind.ServiceRecreated
    Broadcasts.Event.Started -> ProfilesBroadcastSourceEventKind.Started
    is Broadcasts.Event.Stopped -> ProfilesBroadcastSourceEventKind.Stopped
    Broadcasts.Event.ProfileChanged -> ProfilesBroadcastSourceEventKind.ProfileChanged
    is Broadcasts.Event.ProfileUpdateCompleted ->
      ProfilesBroadcastSourceEventKind.ProfileUpdateCompleted
    is Broadcasts.Event.ProfileUpdateFailed -> ProfilesBroadcastSourceEventKind.ProfileUpdateFailed
    Broadcasts.Event.ProfileLoaded -> ProfilesBroadcastSourceEventKind.ProfileLoaded
  }

private fun Broadcasts.Event.profilesUpdateUuid(): Uuid? =
  when (this) {
    is Broadcasts.Event.ProfileUpdateCompleted -> uuid
    is Broadcasts.Event.ProfileUpdateFailed -> uuid
    else -> null
  }

private fun Broadcasts.Event.profilesUpdateReason(): String? =
  when (this) {
    is Broadcasts.Event.ProfileUpdateFailed -> reason
    else -> null
  }
