package com.github.kr328.clash.profile.ui

enum class ProvidersBroadcastEventKind {
  ServiceRecreated,
  Started,
  Stopped,
  ProfileChanged,
  ProfileUpdateCompleted,
  ProfileUpdateFailed,
  ProfileLoaded,
}

internal enum class ProvidersBroadcastSourceEventKind {
  ServiceRecreated,
  Started,
  Stopped,
  ProfileChanged,
  ProfileUpdateCompleted,
  ProfileUpdateFailed,
  ProfileLoaded,
}

data class ProvidersBroadcastEvent(val kind: ProvidersBroadcastEventKind)

internal enum class ProvidersBroadcastAction {
  FetchProviders,
  Ignore,
}

internal fun providersBroadcastAction(event: ProvidersBroadcastEvent): ProvidersBroadcastAction {
  return providersBroadcastAction(event.kind)
}

internal fun providersBroadcastEventFromSource(
  kind: ProvidersBroadcastSourceEventKind
): ProvidersBroadcastEvent {
  return when (kind) {
    ProvidersBroadcastSourceEventKind.ServiceRecreated ->
      ProvidersBroadcastEvent(ProvidersBroadcastEventKind.ServiceRecreated)
    ProvidersBroadcastSourceEventKind.Started ->
      ProvidersBroadcastEvent(ProvidersBroadcastEventKind.Started)
    ProvidersBroadcastSourceEventKind.Stopped ->
      ProvidersBroadcastEvent(ProvidersBroadcastEventKind.Stopped)
    ProvidersBroadcastSourceEventKind.ProfileChanged ->
      ProvidersBroadcastEvent(ProvidersBroadcastEventKind.ProfileChanged)
    ProvidersBroadcastSourceEventKind.ProfileUpdateCompleted ->
      ProvidersBroadcastEvent(ProvidersBroadcastEventKind.ProfileUpdateCompleted)
    ProvidersBroadcastSourceEventKind.ProfileUpdateFailed ->
      ProvidersBroadcastEvent(ProvidersBroadcastEventKind.ProfileUpdateFailed)
    ProvidersBroadcastSourceEventKind.ProfileLoaded ->
      ProvidersBroadcastEvent(ProvidersBroadcastEventKind.ProfileLoaded)
  }
}

internal fun providersBroadcastAction(kind: ProvidersBroadcastEventKind): ProvidersBroadcastAction {
  return when (kind) {
    ProvidersBroadcastEventKind.ProfileLoaded -> ProvidersBroadcastAction.FetchProviders
    ProvidersBroadcastEventKind.ServiceRecreated,
    ProvidersBroadcastEventKind.Started,
    ProvidersBroadcastEventKind.Stopped,
    ProvidersBroadcastEventKind.ProfileChanged,
    ProvidersBroadcastEventKind.ProfileUpdateCompleted,
    ProvidersBroadcastEventKind.ProfileUpdateFailed -> ProvidersBroadcastAction.Ignore
  }
}
