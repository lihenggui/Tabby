package com.github.kr328.clash.profile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.engine.api.ProfileRepository
import kotlin.uuid.Uuid
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.new_profile

@Composable
fun ProfileRepositoryNewProfileRouteContent(
  profileRepository: ProfileRepository,
  onProperties: (Uuid) -> Unit,
  modifier: Modifier = Modifier,
  externalProviders: List<NewProfileRouteExternalProvider> = emptyList(),
  onLaunchQrScanner: () -> Unit = {},
  onCreateExternal: (NewProfileRouteExternalProvider) -> Unit = {},
  onDetailExternal: (NewProfileRouteExternalProvider) -> Unit = {},
  onActionError: (Throwable) -> Unit = {},
) {
  val scope = rememberCoroutineScope()
  val newProfileName = stringResource(SharedRes.string.new_profile)

  fun createProfile(type: Profile.Type) {
    scope.launch {
      runCatching { profileRepository.create(type = type, name = newProfileName) }
        .onSuccess(onProperties)
        .onFailure(onActionError)
    }
  }

  NewProfileRouteContent(
    modifier = modifier,
    externalProviders = externalProviders,
    onCreateBuiltIn = { provider ->
      when (val action = newProfileRouteCreateAction(provider)) {
        is NewProfileRouteCreateAction.CreateProfile -> createProfile(action.type)
        NewProfileRouteCreateAction.LaunchQrScanner -> onLaunchQrScanner()
      }
    },
    onCreateExternal = onCreateExternal,
    onDetailExternal = onDetailExternal,
  )
}
