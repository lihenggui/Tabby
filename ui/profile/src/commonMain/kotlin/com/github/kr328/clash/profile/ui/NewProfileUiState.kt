package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile

internal data class NewProfileUiState<T>(val providers: List<T> = emptyList())

internal enum class NewProfileProviderKind {
  File,
  Url,
  QR,
  External,
}

internal sealed interface NewProfileCreateAction {
  data class CreateProfile(val type: Profile.Type) : NewProfileCreateAction

  data object LaunchQRScanner : NewProfileCreateAction

  data object LaunchExternalProvider : NewProfileCreateAction
}

internal sealed interface NewProfileDetailAction {
  data class OpenAppSettings(val packageName: String) : NewProfileDetailAction

  data object Ignore : NewProfileDetailAction
}

internal fun newProfileCreateAction(kind: NewProfileProviderKind): NewProfileCreateAction {
  return when (kind) {
    NewProfileProviderKind.File -> NewProfileCreateAction.CreateProfile(Profile.Type.File)
    NewProfileProviderKind.Url -> NewProfileCreateAction.CreateProfile(Profile.Type.Url)
    NewProfileProviderKind.QR -> NewProfileCreateAction.LaunchQRScanner
    NewProfileProviderKind.External -> NewProfileCreateAction.LaunchExternalProvider
  }
}

internal fun newProfileDetailAction(packageName: String?): NewProfileDetailAction {
  return if (packageName == null) NewProfileDetailAction.Ignore
  else NewProfileDetailAction.OpenAppSettings(packageName)
}

internal fun <T> NewProfileUiState<T>.withNewProfileProviders(
  providers: List<T>
): NewProfileUiState<T> {
  return copy(providers = providers)
}
