package com.github.kr328.clash.profile.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.engine.api.ProfileRepository
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@Composable
fun ProfileRepositoryProfilesListRouteContent(
  profileRepository: ProfileRepository,
  onCreate: () -> Unit,
  onEdit: (Uuid) -> Unit,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  broadcastEvents: Flow<ProfilesBroadcastEvent> = emptyFlow(),
  formatBytes: (Long) -> String = ::profileBinaryBytesText,
  formatExpire: (Long) -> String = ::profileExpireDateString,
  formatElapsedMillis: @Composable (Long) -> String = { elapsedTimeTextString(it) },
  onPendingActivation: suspend (Profile) -> Unit = { profile -> onEdit(profile.uuid) },
  onProfileUpdateCompleted: suspend (uuid: Uuid, profileName: String?) -> Unit = { _, _ -> },
  onProfileUpdateFailed: suspend (uuid: Uuid, profileName: String?, reason: String?) -> Unit =
    { _, _, _ ->
    },
  onActionError: (Throwable) -> Unit = {},
) {
  val scope = rememberCoroutineScope()
  var profiles by remember(profileRepository) { mutableStateOf(emptyList<Profile>()) }
  var allUpdating by remember { mutableStateOf(false) }
  var currentTimeMillis by remember(profileRepository) { mutableStateOf(tabbyCurrentTimeMillis()) }

  fun launchProfileAction(block: suspend () -> Unit) {
    scope.launch { runCatching { block() }.onFailure(onActionError) }
  }

  suspend fun fetchProfiles() {
    profiles = profileRepository.queryProfiles()
  }

  suspend fun handleBroadcastEvent(event: ProfilesBroadcastEvent) {
    when (val action = profilesBroadcastAction(event)) {
      ProfilesBroadcastAction.FetchProfiles ->
        runCatching { fetchProfiles() }.onFailure(onActionError)
      ProfilesBroadcastAction.Ignore -> Unit
      is ProfilesBroadcastAction.ShowUpdateCompleted ->
        runCatching {
            onProfileUpdateCompleted(
              action.uuid,
              profileRepository.queryByUuid(action.uuid)?.name,
            )
          }
          .onFailure(onActionError)
      is ProfilesBroadcastAction.ShowUpdateFailed ->
        runCatching {
            onProfileUpdateFailed(
              action.uuid,
              profileRepository.queryByUuid(action.uuid)?.name,
              action.reason,
            )
          }
          .onFailure(onActionError)
    }
  }

  LaunchedEffect(profileRepository) {
    profileRepository.observeProfiles().collect { profiles = it }
  }
  LaunchedEffect(profileRepository, broadcastEvents) {
    broadcastEvents.collect { event -> handleBroadcastEvent(event) }
  }
  LaunchedEffect(profileRepository) {
    while (true) {
      delay(1.minutes)
      currentTimeMillis = tabbyCurrentTimeMillis()
    }
  }

  ProfilesListRouteContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    profiles = profiles,
    allUpdating = allUpdating,
    currentTimeMillis = currentTimeMillis,
    formatBytes = formatBytes,
    formatExpire = formatExpire,
    formatElapsedMillis = formatElapsedMillis,
    onUpdateAll = {
      when (profileUpdateAllAction(allUpdating)) {
        ProfileUpdateAllAction.QueryProfiles -> {
          launchProfileAction {
            allUpdating = true
            try {
              profileUpdateAllTargets(profileRepository.queryProfiles()).forEach { uuid ->
                profileRepository.update(uuid)
              }
            } finally {
              allUpdating = false
            }
          }
        }
        ProfileUpdateAllAction.Ignore -> Unit
      }
    },
    onCreate = onCreate,
    onActivate = { profile ->
      if (profile.imported) {
        launchProfileAction { profileRepository.setActive(profile) }
      } else {
        launchProfileAction { onPendingActivation(profile) }
      }
    },
    onUpdate = { profile -> launchProfileAction { profileRepository.update(profile.uuid) } },
    onEdit = { profile -> onEdit(profile.uuid) },
    onDuplicate = { profile ->
      launchProfileAction {
        val uuid = profileRepository.clone(profile.uuid)

        onEdit(uuid)
      }
    },
    onDelete = { profile -> launchProfileAction { profileRepository.delete(profile.uuid) } },
  )
}
