package com.github.kr328.clash.profile.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.engine.api.ProfileRepository
import kotlin.uuid.Uuid
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@Composable
fun ProfileRepositoryPropertiesRouteContent(
  profileRepository: ProfileRepository,
  uuid: Uuid,
  onBrowseFiles: (Uuid) -> Unit,
  onFinish: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  tipsProperties: AnnotatedString = AnnotatedString("Accept Only Tabby Config"),
  autoSaveEvents: Flow<Unit> = emptyFlow(),
  onActionError: (Throwable) -> Unit = {},
) {
  var routeProfile by remember(profileRepository, uuid) { mutableStateOf<Profile?>(null) }
  var editedProfile by remember(profileRepository, uuid) { mutableStateOf<Profile?>(null) }
  var savedProfile by remember(profileRepository, uuid) { mutableStateOf<Profile?>(null) }
  var canceled by remember(profileRepository, uuid) { mutableStateOf(false) }

  suspend fun saveProfile(profile: Profile) {
    profileRepository.patch(
      uuid = profile.uuid,
      name = profile.name,
      source = profile.source,
      interval = profile.interval,
    )
    savedProfile = profile
  }

  suspend fun autoSaveProfile() {
    when (
      val action =
        propertiesAutoSaveAction(
          canceled = canceled,
          state = propertiesAutoSaveUiState(editedProfile, savedProfile),
        )
    ) {
      is PropertiesAutoSaveAction.Save ->
        runCatching {
            saveProfile(action.profile)
            routeProfile = action.profile
          }
          .onFailure(onActionError)
      PropertiesAutoSaveAction.Ignore -> Unit
    }
  }

  fun finish(success: Boolean) {
    canceled = true
    onFinish(success)
  }

  LaunchedEffect(profileRepository, uuid) {
    try {
      val loadedProfile =
        runCatching { profileRepository.queryByUuid(uuid) }.onFailure(onActionError).getOrNull()
      if (loadedProfile == null) {
        finish(false)
        return@LaunchedEffect
      }

      routeProfile = loadedProfile
      editedProfile = loadedProfile
      savedProfile = loadedProfile
      awaitCancellation()
    } finally {
      runCatching { profileRepository.release(uuid) }.onFailure(onActionError)
    }
  }
  LaunchedEffect(profileRepository, uuid, autoSaveEvents) {
    autoSaveEvents.collect { autoSaveProfile() }
  }

  routeProfile?.let { loadedProfile ->
    PropertiesRouteContent(
      modifier = modifier,
      profile = loadedProfile,
      snackbarHostState = snackbarHostState,
      tipsProperties = tipsProperties,
      onBrowseFiles = { onBrowseFiles(it.uuid) },
      onCommit = { committedProfile, updateStatus ->
        try {
          saveProfile(committedProfile)
          coroutineScope {
            profileRepository.commit(committedProfile.uuid) { status ->
              launch { updateStatus(status) }
            }
          }
        } catch (cause: CancellationException) {
          throw cause
        } catch (cause: Exception) {
          onActionError(cause)
          throw cause
        }
      },
      onProfileChange = { profile -> editedProfile = profile },
      onFinish = ::finish,
    )
  }
}
