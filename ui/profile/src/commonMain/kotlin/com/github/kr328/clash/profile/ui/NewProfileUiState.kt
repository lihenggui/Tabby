package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile
import kotlin.uuid.Uuid

internal data class NewProfileUiState<T>(val providers: List<T> = emptyList())

internal fun <T> newProfileInitialUiState(): NewProfileUiState<T> {
  return NewProfileUiState()
}

internal enum class NewProfileProviderKind {
  File,
  Url,
  QR,
  External,
}

internal fun newProfileBuiltInProviderKinds(): List<NewProfileProviderKind> {
  return listOf(
    NewProfileProviderKind.File,
    NewProfileProviderKind.Url,
    NewProfileProviderKind.QR,
  )
}

internal enum class NewProfileProviderTextToken {
  File,
  Url,
  Qr,
  ImportFromFile,
  ImportFromUrl,
  ImportFromQr,
}

internal enum class NewProfileProviderGraphicToken {
  File,
  Url,
  Qr,
}

internal data class NewProfileBuiltInProviderPresentation(
  val nameToken: NewProfileProviderTextToken,
  val summaryToken: NewProfileProviderTextToken,
  val graphicToken: NewProfileProviderGraphicToken,
)

internal fun newProfileBuiltInProviderPresentation(
  kind: NewProfileProviderKind
): NewProfileBuiltInProviderPresentation? {
  return when (kind) {
    NewProfileProviderKind.File ->
      NewProfileBuiltInProviderPresentation(
        nameToken = NewProfileProviderTextToken.File,
        summaryToken = NewProfileProviderTextToken.ImportFromFile,
        graphicToken = NewProfileProviderGraphicToken.File,
      )
    NewProfileProviderKind.Url ->
      NewProfileBuiltInProviderPresentation(
        nameToken = NewProfileProviderTextToken.Url,
        summaryToken = NewProfileProviderTextToken.ImportFromUrl,
        graphicToken = NewProfileProviderGraphicToken.Url,
      )
    NewProfileProviderKind.QR ->
      NewProfileBuiltInProviderPresentation(
        nameToken = NewProfileProviderTextToken.Qr,
        summaryToken = NewProfileProviderTextToken.ImportFromQr,
        graphicToken = NewProfileProviderGraphicToken.Qr,
      )
    NewProfileProviderKind.External -> null
  }
}

internal fun <T> newProfileProviderList(
  externalProviders: Iterable<T>,
  builtInProvider: (NewProfileProviderKind) -> T?,
): List<T> {
  return newProfileBuiltInProviderKinds().mapNotNull(builtInProvider) + externalProviders
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

internal sealed interface NewProfileEventState<out ExternalProviderT, out AppSettingsTargetT> {
  data object Idle : NewProfileEventState<Nothing, Nothing>

  data object LaunchQRScanner : NewProfileEventState<Nothing, Nothing>

  data class LaunchExternalProvider<out ExternalProviderT>(
    val externalProvider: ExternalProviderT
  ) : NewProfileEventState<ExternalProviderT, Nothing>

  data class LaunchProperties(val uuid: Uuid) : NewProfileEventState<Nothing, Nothing>

  data class OpenAppSettings<out AppSettingsTargetT>(val target: AppSettingsTargetT) :
    NewProfileEventState<Nothing, AppSettingsTargetT>

  data class ShowMessage(val message: String) : NewProfileEventState<Nothing, Nothing>

  data object Finish : NewProfileEventState<Nothing, Nothing>
}

internal fun <ExternalProviderT, AppSettingsTargetT> newProfileInitialEventState():
  NewProfileEventState<ExternalProviderT, AppSettingsTargetT> {
  return NewProfileEventState.Idle
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

internal fun newProfileExternalProviderResultAcceptedFromPlatformResultCode(
  resultCode: Int,
  acceptedResultCode: Int,
): Boolean {
  return resultCode == acceptedResultCode
}

internal fun <ExternalProviderT> newProfileCreateEventState(
  action: NewProfileCreateAction,
  externalProvider: ExternalProviderT? = null,
): NewProfileEventState<ExternalProviderT, Nothing>? {
  return when (action) {
    is NewProfileCreateAction.CreateProfile -> null
    NewProfileCreateAction.LaunchQRScanner -> NewProfileEventState.LaunchQRScanner
    NewProfileCreateAction.LaunchExternalProvider ->
      externalProvider?.let { NewProfileEventState.LaunchExternalProvider(it) }
  }
}

internal fun <AppSettingsTargetT> newProfileDetailEventState(
  action: NewProfileDetailAction,
  appSettingsTarget: AppSettingsTargetT,
): NewProfileEventState<Nothing, AppSettingsTargetT>? {
  return when (action) {
    is NewProfileDetailAction.OpenAppSettings ->
      NewProfileEventState.OpenAppSettings(appSettingsTarget)
    NewProfileDetailAction.Ignore -> null
  }
}

internal fun newProfileQrEventState(
  action: ProfileQrAction,
  missingPermissionMessage: String,
  scanErrorMessage: String,
): NewProfileEventState<Nothing, Nothing>? {
  return when (action) {
    is ProfileQrAction.CreateUrlProfile -> null
    ProfileQrAction.Ignore -> null
    ProfileQrAction.ShowMissingPermission ->
      NewProfileEventState.ShowMessage(missingPermissionMessage)
    ProfileQrAction.ShowScanError -> NewProfileEventState.ShowMessage(scanErrorMessage)
  }
}

internal fun newProfileLaunchPropertiesEventState(
  uuid: Uuid
): NewProfileEventState<Nothing, Nothing> {
  return NewProfileEventState.LaunchProperties(uuid)
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

internal fun newProfileErrorEventState(
  message: String?,
  unknownMessage: String,
): NewProfileEventState<Nothing, Nothing> {
  return NewProfileEventState.ShowMessage(message ?: unknownMessage)
}

internal fun newProfileConsumedEventState(): NewProfileEventState<Nothing, Nothing> {
  return NewProfileEventState.Idle
}
