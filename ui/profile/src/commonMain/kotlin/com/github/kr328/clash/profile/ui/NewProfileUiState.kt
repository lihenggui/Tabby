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

internal data class NewProfileExternalProviderPresentation(
  val key: String,
  val name: String,
  val summary: String,
  val hasDetail: Boolean,
)

internal fun <T> newProfileProviderTextPlatformToken(
  token: NewProfileProviderTextToken,
  file: T,
  url: T,
  qr: T,
  importFromFile: T,
  importFromUrl: T,
  importFromQr: T,
): T {
  return when (token) {
    NewProfileProviderTextToken.File -> file
    NewProfileProviderTextToken.Url -> url
    NewProfileProviderTextToken.Qr -> qr
    NewProfileProviderTextToken.ImportFromFile -> importFromFile
    NewProfileProviderTextToken.ImportFromUrl -> importFromUrl
    NewProfileProviderTextToken.ImportFromQr -> importFromQr
  }
}

internal fun <T> newProfileProviderGraphicPlatformToken(
  token: NewProfileProviderGraphicToken,
  file: T,
  url: T,
  qr: T,
): T {
  return when (token) {
    NewProfileProviderGraphicToken.File -> file
    NewProfileProviderGraphicToken.Url -> url
    NewProfileProviderGraphicToken.Qr -> qr
  }
}

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

internal sealed interface NewProfileExternalProviderResultAction<out SourceT : Any> {
  data class CreateProfile<SourceT : Any>(val source: SourceT, val name: String?) :
    NewProfileExternalProviderResultAction<SourceT>

  data object Ignore : NewProfileExternalProviderResultAction<Nothing>
}

internal data class NewProfileExternalProviderResult<out SourceT : Any>(
  val resultAccepted: Boolean,
  val source: SourceT?,
  val name: String?,
)

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

internal sealed interface NewProfileEventRouteEffect<
  out ExternalProviderT,
  out AppSettingsTargetT,
> {
  data object Ignore : NewProfileEventRouteEffect<Nothing, Nothing>

  data object LaunchQRScanner : NewProfileEventRouteEffect<Nothing, Nothing>

  data class LaunchExternalProvider<out ExternalProviderT>(
    val externalProvider: ExternalProviderT
  ) : NewProfileEventRouteEffect<ExternalProviderT, Nothing>

  data class LaunchProperties(val uuid: Uuid) : NewProfileEventRouteEffect<Nothing, Nothing>

  data class OpenAppSettings<out AppSettingsTargetT>(val target: AppSettingsTargetT) :
    NewProfileEventRouteEffect<Nothing, AppSettingsTargetT>

  data class ShowMessage(val message: String) : NewProfileEventRouteEffect<Nothing, Nothing>

  data object Finish : NewProfileEventRouteEffect<Nothing, Nothing>
}

internal fun <ExternalProviderT, AppSettingsTargetT> newProfileInitialEventState():
  NewProfileEventState<ExternalProviderT, AppSettingsTargetT> {
  return NewProfileEventState.Idle
}

internal fun <ExternalProviderT, AppSettingsTargetT> newProfileEventRouteEffect(
  eventState: NewProfileEventState<ExternalProviderT, AppSettingsTargetT>
): NewProfileEventRouteEffect<ExternalProviderT, AppSettingsTargetT> {
  return when (eventState) {
    NewProfileEventState.Idle -> NewProfileEventRouteEffect.Ignore
    NewProfileEventState.LaunchQRScanner -> NewProfileEventRouteEffect.LaunchQRScanner
    is NewProfileEventState.LaunchExternalProvider ->
      NewProfileEventRouteEffect.LaunchExternalProvider(eventState.externalProvider)
    is NewProfileEventState.LaunchProperties ->
      NewProfileEventRouteEffect.LaunchProperties(eventState.uuid)
    is NewProfileEventState.OpenAppSettings ->
      NewProfileEventRouteEffect.OpenAppSettings(eventState.target)
    is NewProfileEventState.ShowMessage ->
      NewProfileEventRouteEffect.ShowMessage(eventState.message)
    NewProfileEventState.Finish -> NewProfileEventRouteEffect.Finish
  }
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

internal fun <SourceT : Any> newProfileExternalProviderResultAction(
  result: NewProfileExternalProviderResult<SourceT>
): NewProfileExternalProviderResultAction<SourceT> {
  return newProfileExternalProviderResultAction(
    resultAccepted = result.resultAccepted,
    source = result.source,
    name = result.name,
  )
}

internal fun <SourceT : Any> newProfileExternalProviderResultAction(
  resultAccepted: Boolean,
  source: SourceT?,
  name: String?,
): NewProfileExternalProviderResultAction<SourceT> {
  return if (resultAccepted && source != null) {
    NewProfileExternalProviderResultAction.CreateProfile(source = source, name = name)
  } else {
    NewProfileExternalProviderResultAction.Ignore
  }
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
