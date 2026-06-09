package com.github.kr328.clash.profile.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.engine.api.ProfileRepository
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tabby.ui.profile.generated.resources.Res as ProfileRes
import tabby.ui.profile.generated.resources.import_from_qr_exception
import tabby.ui.profile.generated.resources.import_from_qr_no_permission
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.new_profile
import tabby.ui.shared.generated.resources.unknown

data class NewProfileCreateRequest(
  val type: Profile.Type,
  val name: String? = null,
  val source: String = "",
)

internal sealed interface NewProfileQrScanAction {
  data class CreateProfile(val request: NewProfileCreateRequest) : NewProfileQrScanAction

  data class ShowMessage(val message: String) : NewProfileQrScanAction

  data object Ignore : NewProfileQrScanAction
}

@Composable
fun ProfileRepositoryNewProfileRouteContent(
  profileRepository: ProfileRepository,
  onProperties: (Uuid) -> Unit,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  createRequests: Flow<NewProfileCreateRequest> = emptyFlow(),
  qrScanResults: Flow<ProfileQrScanResult> = emptyFlow(),
  externalProviders: List<NewProfileRouteExternalProvider> = emptyList(),
  onLaunchQrScanner: () -> Unit = {},
  onCreateExternal: (NewProfileRouteExternalProvider) -> Unit = {},
  onDetailExternal: (NewProfileRouteExternalProvider) -> Unit = {},
  onActionError: (Throwable) -> Unit = {},
) {
  val scope = rememberCoroutineScope()
  val newProfileName = stringResource(SharedRes.string.new_profile)
  val unknownMessage = stringResource(SharedRes.string.unknown)
  val missingPermissionMessage = stringResource(ProfileRes.string.import_from_qr_no_permission)
  val scanErrorMessage = stringResource(ProfileRes.string.import_from_qr_exception)

  suspend fun createProfile(request: NewProfileCreateRequest) {
    try {
      val uuid =
        profileRepository.create(
          type = request.type,
          name = newProfileCreateProfileName(request, newProfileName),
          source = request.source,
        )

      onProperties(uuid)
    } catch (cause: Throwable) {
      onActionError(cause)
      snackbarHostState.showSnackbar(cause.message ?: unknownMessage)
    }
  }

  suspend fun handleQrScanResult(result: ProfileQrScanResult) {
    when (
      val action =
        newProfileQrScanAction(
          result = result,
          missingPermissionMessage = missingPermissionMessage,
          scanErrorMessage = scanErrorMessage,
        )
    ) {
      is NewProfileQrScanAction.CreateProfile -> createProfile(action.request)
      is NewProfileQrScanAction.ShowMessage -> snackbarHostState.showSnackbar(action.message)
      NewProfileQrScanAction.Ignore -> Unit
    }
  }

  fun launchCreateProfile(request: NewProfileCreateRequest) {
    scope.launch {
      createProfile(request)
    }
  }

  LaunchedEffect(profileRepository, createRequests) {
    createRequests.collect { request -> createProfile(request) }
  }

  LaunchedEffect(
    profileRepository,
    qrScanResults,
    missingPermissionMessage,
    scanErrorMessage,
  ) {
    qrScanResults.collect { result -> handleQrScanResult(result) }
  }

  NewProfileRouteContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    externalProviders = externalProviders,
    onCreateBuiltIn = { provider ->
      when (val action = newProfileRouteCreateAction(provider)) {
        is NewProfileRouteCreateAction.CreateProfile ->
          launchCreateProfile(NewProfileCreateRequest(action.type))
        NewProfileRouteCreateAction.LaunchQrScanner -> onLaunchQrScanner()
      }
    },
    onCreateExternal = onCreateExternal,
    onDetailExternal = onDetailExternal,
  )
}

internal fun newProfileCreateProfileName(
  request: NewProfileCreateRequest,
  defaultName: String,
): String {
  return request.name ?: defaultName
}

internal fun newProfileCreateRequestFromQrAction(
  action: ProfileQrAction
): NewProfileCreateRequest? {
  return when (action) {
    is ProfileQrAction.CreateUrlProfile ->
      NewProfileCreateRequest(type = Profile.Type.Url, source = action.source)
    ProfileQrAction.Ignore,
    ProfileQrAction.ShowMissingPermission,
    ProfileQrAction.ShowScanError -> null
  }
}

internal fun newProfileQrScanAction(
  result: ProfileQrScanResult,
  missingPermissionMessage: String,
  scanErrorMessage: String,
): NewProfileQrScanAction {
  return newProfileQrScanAction(
    action = profileQrAction(result),
    missingPermissionMessage = missingPermissionMessage,
    scanErrorMessage = scanErrorMessage,
  )
}

internal fun newProfileQrScanAction(
  action: ProfileQrAction,
  missingPermissionMessage: String,
  scanErrorMessage: String,
): NewProfileQrScanAction {
  return when (action) {
    is ProfileQrAction.CreateUrlProfile ->
      NewProfileQrScanAction.CreateProfile(
        NewProfileCreateRequest(
          type = Profile.Type.Url,
          source = action.source,
        )
      )
    ProfileQrAction.Ignore -> NewProfileQrScanAction.Ignore
    ProfileQrAction.ShowMissingPermission ->
      NewProfileQrScanAction.ShowMessage(missingPermissionMessage)
    ProfileQrAction.ShowScanError -> NewProfileQrScanAction.ShowMessage(scanErrorMessage)
  }
}

internal fun <SourceT : Any> newProfileCreateRequestFromExternalProviderResultAction(
  action: NewProfileExternalProviderResultAction<SourceT>,
  sourceText: (SourceT) -> String,
): NewProfileCreateRequest? {
  return when (action) {
    is NewProfileExternalProviderResultAction.CreateProfile ->
      NewProfileCreateRequest(
        type = Profile.Type.External,
        name = action.name,
        source = sourceText(action.source),
      )
    NewProfileExternalProviderResultAction.Ignore -> null
  }
}

internal fun <SourceT : Any> newProfileCreateRequestFromExternalProviderResult(
  result: NewProfileExternalProviderResult<SourceT>,
  sourceText: (SourceT) -> String,
): NewProfileCreateRequest? {
  return newProfileCreateRequestFromExternalProviderResultAction(
    action = newProfileExternalProviderResultAction(result),
    sourceText = sourceText,
  )
}
