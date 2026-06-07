package com.github.kr328.clash.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.engine.api.ProfileRepository
import com.github.kr328.clash.profile.ui.NewProfileRouteBuiltInProvider
import com.github.kr328.clash.profile.ui.NewProfileRouteContent
import com.github.kr328.clash.profile.ui.NewProfileRouteExternalProvider
import kotlin.uuid.Uuid
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.new_profile

@Composable
internal fun ProfileRepositoryNewProfileRouteContent(
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
      when (val action = provider.toTabbyNewProfileCreateAction()) {
        is TabbyNewProfileCreateAction.CreateProfile -> createProfile(action.type)
        TabbyNewProfileCreateAction.LaunchQrScanner -> onLaunchQrScanner()
      }
    },
    onCreateExternal = onCreateExternal,
    onDetailExternal = onDetailExternal,
  )
}

internal sealed interface TabbyNewProfileCreateAction {
  data class CreateProfile(val type: Profile.Type) : TabbyNewProfileCreateAction

  data object LaunchQrScanner : TabbyNewProfileCreateAction
}

internal fun NewProfileRouteBuiltInProvider.toTabbyNewProfileCreateAction():
  TabbyNewProfileCreateAction {
  return when (this) {
    NewProfileRouteBuiltInProvider.File ->
      TabbyNewProfileCreateAction.CreateProfile(Profile.Type.File)
    NewProfileRouteBuiltInProvider.Url ->
      TabbyNewProfileCreateAction.CreateProfile(Profile.Type.Url)
    NewProfileRouteBuiltInProvider.QR -> TabbyNewProfileCreateAction.LaunchQrScanner
  }
}
