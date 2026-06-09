package com.github.kr328.clash.profile.ui

import kotlin.uuid.Uuid

enum class ProfilesBroadcastEventKind {
  ServiceRecreated,
  Started,
  Stopped,
  ProfileChanged,
  ProfileUpdateCompleted,
  ProfileUpdateFailed,
  ProfileLoaded,
}

internal enum class ProfilesBroadcastSourceEventKind {
  ServiceRecreated,
  Started,
  Stopped,
  ProfileChanged,
  ProfileUpdateCompleted,
  ProfileUpdateFailed,
  ProfileLoaded,
}

data class ProfilesBroadcastEvent(
  val kind: ProfilesBroadcastEventKind,
  val uuid: Uuid? = null,
  val reason: String? = null,
)

internal sealed interface ProfilesBroadcastAction {
  data object FetchProfiles : ProfilesBroadcastAction

  data class ShowUpdateCompleted(val uuid: Uuid) : ProfilesBroadcastAction

  data class ShowUpdateFailed(val uuid: Uuid, val reason: String?) : ProfilesBroadcastAction

  data object Ignore : ProfilesBroadcastAction
}

internal fun profilesBroadcastAction(event: ProfilesBroadcastEvent): ProfilesBroadcastAction {
  return profilesBroadcastAction(
    kind = event.kind,
    uuid = event.uuid,
    reason = event.reason,
  )
}

internal fun profilesBroadcastEventFromSource(
  kind: ProfilesBroadcastSourceEventKind,
  uuid: Uuid? = null,
  reason: String? = null,
): ProfilesBroadcastEvent {
  return when (kind) {
    ProfilesBroadcastSourceEventKind.ServiceRecreated ->
      ProfilesBroadcastEvent(ProfilesBroadcastEventKind.ServiceRecreated)
    ProfilesBroadcastSourceEventKind.Started ->
      ProfilesBroadcastEvent(ProfilesBroadcastEventKind.Started)
    ProfilesBroadcastSourceEventKind.Stopped ->
      ProfilesBroadcastEvent(ProfilesBroadcastEventKind.Stopped)
    ProfilesBroadcastSourceEventKind.ProfileChanged ->
      ProfilesBroadcastEvent(ProfilesBroadcastEventKind.ProfileChanged)
    ProfilesBroadcastSourceEventKind.ProfileUpdateCompleted ->
      ProfilesBroadcastEvent(
        kind = ProfilesBroadcastEventKind.ProfileUpdateCompleted,
        uuid = uuid,
      )
    ProfilesBroadcastSourceEventKind.ProfileUpdateFailed ->
      ProfilesBroadcastEvent(
        kind = ProfilesBroadcastEventKind.ProfileUpdateFailed,
        uuid = uuid,
        reason = reason,
      )
    ProfilesBroadcastSourceEventKind.ProfileLoaded ->
      ProfilesBroadcastEvent(ProfilesBroadcastEventKind.ProfileLoaded)
  }
}

internal fun <T> profilesBroadcastEventFromPlatformPayload(
  event: T,
  kind: (T) -> ProfilesBroadcastSourceEventKind,
  uuid: (T) -> Uuid? = { null },
  reason: (T) -> String? = { null },
): ProfilesBroadcastEvent {
  return profilesBroadcastEventFromSource(
    kind = kind(event),
    uuid = uuid(event),
    reason = reason(event),
  )
}

internal fun profilesBroadcastAction(
  kind: ProfilesBroadcastEventKind,
  uuid: Uuid? = null,
  reason: String? = null,
): ProfilesBroadcastAction {
  return when (kind) {
    ProfilesBroadcastEventKind.ServiceRecreated,
    ProfilesBroadcastEventKind.Started,
    ProfilesBroadcastEventKind.ProfileChanged,
    ProfilesBroadcastEventKind.ProfileLoaded -> ProfilesBroadcastAction.FetchProfiles
    ProfilesBroadcastEventKind.Stopped -> ProfilesBroadcastAction.Ignore
    ProfilesBroadcastEventKind.ProfileUpdateCompleted ->
      uuid?.let(ProfilesBroadcastAction::ShowUpdateCompleted) ?: ProfilesBroadcastAction.Ignore
    ProfilesBroadcastEventKind.ProfileUpdateFailed ->
      uuid?.let { ProfilesBroadcastAction.ShowUpdateFailed(it, reason) }
        ?: ProfilesBroadcastAction.Ignore
  }
}
