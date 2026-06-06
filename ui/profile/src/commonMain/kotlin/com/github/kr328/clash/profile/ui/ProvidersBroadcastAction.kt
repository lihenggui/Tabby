package com.github.kr328.clash.profile.ui

internal enum class ProvidersBroadcastEventKind {
  ServiceRecreated,
  Started,
  Stopped,
  ProfileChanged,
  ProfileUpdateCompleted,
  ProfileUpdateFailed,
  ProfileLoaded,
}

internal enum class ProvidersBroadcastAction {
  FetchProviders,
  Ignore,
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
