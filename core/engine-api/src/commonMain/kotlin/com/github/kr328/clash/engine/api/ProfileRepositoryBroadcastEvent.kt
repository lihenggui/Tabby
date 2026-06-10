package com.github.kr328.clash.engine.api

enum class ProfileRepositoryBroadcastEventKind {
  ServiceRecreated,
  Started,
  Stopped,
  ProfileChanged,
  ProfileUpdateCompleted,
  ProfileUpdateFailed,
  ProfileLoaded,
}

fun profileRepositoryRequiresProfileSnapshotRefresh(
  kind: ProfileRepositoryBroadcastEventKind
): Boolean =
  when (kind) {
    ProfileRepositoryBroadcastEventKind.ProfileChanged,
    ProfileRepositoryBroadcastEventKind.ProfileUpdateCompleted,
    ProfileRepositoryBroadcastEventKind.ProfileUpdateFailed,
    ProfileRepositoryBroadcastEventKind.ProfileLoaded -> true
    ProfileRepositoryBroadcastEventKind.ServiceRecreated,
    ProfileRepositoryBroadcastEventKind.Started,
    ProfileRepositoryBroadcastEventKind.Stopped -> false
  }

fun <T> profileRepositoryRequiresProfileSnapshotRefreshFromPlatformPayload(
  event: T,
  kind: (T) -> ProfileRepositoryBroadcastEventKind,
): Boolean = profileRepositoryRequiresProfileSnapshotRefresh(kind(event))
