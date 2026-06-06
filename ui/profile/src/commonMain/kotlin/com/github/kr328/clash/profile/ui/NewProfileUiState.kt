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

internal sealed interface NewProfileExternalProviderResultAction {
  data class CreateProfile(val name: String?) : NewProfileExternalProviderResultAction

  data object Ignore : NewProfileExternalProviderResultAction
}

internal sealed interface NewProfileProviderSelectionAction<out T> {
  data class SelectProvider<T>(val provider: T) : NewProfileProviderSelectionAction<T>

  data object Ignore : NewProfileProviderSelectionAction<Nothing>
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

internal fun newProfileExternalProviderResultAction(
  resultAccepted: Boolean,
  sourceSelected: Boolean,
  name: String?,
): NewProfileExternalProviderResultAction {
  return if (resultAccepted && sourceSelected) {
    NewProfileExternalProviderResultAction.CreateProfile(name)
  } else {
    NewProfileExternalProviderResultAction.Ignore
  }
}

internal fun <T> newProfileProviderSelectionAction(
  providers: List<T>,
  index: Int,
): NewProfileProviderSelectionAction<T> {
  val provider = providers.getOrNull(index) ?: return NewProfileProviderSelectionAction.Ignore

  return NewProfileProviderSelectionAction.SelectProvider(provider)
}

internal fun <T, R : T> newProfileProviderDetailSelectionAction(
  providers: List<T>,
  index: Int,
  detailProvider: (T) -> R?,
): NewProfileProviderSelectionAction<R> {
  val provider = providers.getOrNull(index) ?: return NewProfileProviderSelectionAction.Ignore
  val selected = detailProvider(provider) ?: return NewProfileProviderSelectionAction.Ignore

  return NewProfileProviderSelectionAction.SelectProvider(selected)
}

internal fun <T> NewProfileUiState<T>.withNewProfileProviders(
  providers: List<T>
): NewProfileUiState<T> {
  return copy(providers = providers)
}
