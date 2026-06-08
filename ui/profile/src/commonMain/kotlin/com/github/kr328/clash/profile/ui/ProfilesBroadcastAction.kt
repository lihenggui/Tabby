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

internal enum class ProfilesPlatformBroadcastEventKind {
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

internal fun profilesBroadcastEventFromPlatformPayload(
  kind: ProfilesPlatformBroadcastEventKind,
  uuid: Uuid? = null,
  reason: String? = null,
): ProfilesBroadcastEvent {
  val eventKind =
    when (kind) {
      ProfilesPlatformBroadcastEventKind.ServiceRecreated ->
        ProfilesBroadcastEventKind.ServiceRecreated
      ProfilesPlatformBroadcastEventKind.Started -> ProfilesBroadcastEventKind.Started
      ProfilesPlatformBroadcastEventKind.Stopped -> ProfilesBroadcastEventKind.Stopped
      ProfilesPlatformBroadcastEventKind.ProfileChanged -> ProfilesBroadcastEventKind.ProfileChanged
      ProfilesPlatformBroadcastEventKind.ProfileUpdateCompleted ->
        ProfilesBroadcastEventKind.ProfileUpdateCompleted
      ProfilesPlatformBroadcastEventKind.ProfileUpdateFailed ->
        ProfilesBroadcastEventKind.ProfileUpdateFailed
      ProfilesPlatformBroadcastEventKind.ProfileLoaded -> ProfilesBroadcastEventKind.ProfileLoaded
    }

  return when (eventKind) {
    ProfilesBroadcastEventKind.ProfileUpdateCompleted ->
      ProfilesBroadcastEvent(kind = eventKind, uuid = uuid)
    ProfilesBroadcastEventKind.ProfileUpdateFailed ->
      ProfilesBroadcastEvent(kind = eventKind, uuid = uuid, reason = reason)
    ProfilesBroadcastEventKind.ServiceRecreated,
    ProfilesBroadcastEventKind.Started,
    ProfilesBroadcastEventKind.Stopped,
    ProfilesBroadcastEventKind.ProfileChanged,
    ProfilesBroadcastEventKind.ProfileLoaded -> ProfilesBroadcastEvent(eventKind)
  }
}

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
