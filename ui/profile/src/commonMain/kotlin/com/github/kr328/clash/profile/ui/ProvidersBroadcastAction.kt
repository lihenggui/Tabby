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

internal enum class ProvidersPlatformBroadcastEventKind {
  ServiceRecreated,
  Started,
  Stopped,
  ProfileChanged,
  ProfileUpdateCompleted,
  ProfileUpdateFailed,
  ProfileLoaded,
}

data class ProvidersBroadcastEvent(val kind: ProvidersBroadcastEventKind)

internal fun providersBroadcastEventFromPlatformPayload(
  kind: ProvidersPlatformBroadcastEventKind
): ProvidersBroadcastEvent {
  val eventKind =
    when (kind) {
      ProvidersPlatformBroadcastEventKind.ServiceRecreated ->
        ProvidersBroadcastEventKind.ServiceRecreated
      ProvidersPlatformBroadcastEventKind.Started -> ProvidersBroadcastEventKind.Started
      ProvidersPlatformBroadcastEventKind.Stopped -> ProvidersBroadcastEventKind.Stopped
      ProvidersPlatformBroadcastEventKind.ProfileChanged ->
        ProvidersBroadcastEventKind.ProfileChanged
      ProvidersPlatformBroadcastEventKind.ProfileUpdateCompleted ->
        ProvidersBroadcastEventKind.ProfileUpdateCompleted
      ProvidersPlatformBroadcastEventKind.ProfileUpdateFailed ->
        ProvidersBroadcastEventKind.ProfileUpdateFailed
      ProvidersPlatformBroadcastEventKind.ProfileLoaded -> ProvidersBroadcastEventKind.ProfileLoaded
    }

  return ProvidersBroadcastEvent(eventKind)
}

internal enum class ProvidersBroadcastAction {
  FetchProviders,
  Ignore,
}

internal fun providersBroadcastAction(event: ProvidersBroadcastEvent): ProvidersBroadcastAction {
  return providersBroadcastAction(event.kind)
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
