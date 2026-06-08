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
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.new_profile
import tabby.ui.shared.generated.resources.unknown

data class NewProfileCreateRequest(
  val type: Profile.Type,
  val name: String? = null,
  val source: String = "",
)

@Composable
fun ProfileRepositoryNewProfileRouteContent(
  profileRepository: ProfileRepository,
  onProperties: (Uuid) -> Unit,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  createRequests: Flow<NewProfileCreateRequest> = emptyFlow(),
  externalProviders: List<NewProfileRouteExternalProvider> = emptyList(),
  onLaunchQrScanner: () -> Unit = {},
  onCreateExternal: (NewProfileRouteExternalProvider) -> Unit = {},
  onDetailExternal: (NewProfileRouteExternalProvider) -> Unit = {},
  onActionError: (Throwable) -> Unit = {},
) {
  val scope = rememberCoroutineScope()
  val newProfileName = stringResource(SharedRes.string.new_profile)
  val unknownMessage = stringResource(SharedRes.string.unknown)

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

  fun launchCreateProfile(request: NewProfileCreateRequest) {
    scope.launch {
      createProfile(request)
    }
  }

  LaunchedEffect(profileRepository, createRequests) {
    createRequests.collect { request -> createProfile(request) }
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

internal fun newProfileCreateRequestFromExternalProviderResultAction(
  action: NewProfileExternalProviderResultAction,
  source: String,
): NewProfileCreateRequest? {
  return when (action) {
    is NewProfileExternalProviderResultAction.CreateProfile ->
      NewProfileCreateRequest(type = Profile.Type.External, name = action.name, source = source)
    NewProfileExternalProviderResultAction.Ignore -> null
  }
}
