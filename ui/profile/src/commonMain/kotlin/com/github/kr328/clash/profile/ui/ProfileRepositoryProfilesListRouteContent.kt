package com.github.kr328.clash.profile.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
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
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import tabby.ui.profile.generated.resources.Res as ProfileRes
import tabby.ui.profile.generated.resources.active_unsaved_tips
import tabby.ui.profile.generated.resources.edit
import tabby.ui.profile.generated.resources.toast_profile_updated_complete
import tabby.ui.profile.generated.resources.toast_profile_updated_failed
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.unknown

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
  onActionError: (Throwable) -> Unit = {},
) {
  val scope = rememberCoroutineScope()
  val activeUnsavedTipsMessage = stringResource(ProfileRes.string.active_unsaved_tips)
  val editActionLabel = stringResource(ProfileRes.string.edit)
  val unknownMessage = stringResource(SharedRes.string.unknown)
  val currentOnCreate = rememberUpdatedState(onCreate)
  val currentOnEdit = rememberUpdatedState(onEdit)
  var profiles by remember(profileRepository) { mutableStateOf(emptyList<Profile>()) }
  var allUpdating by remember { mutableStateOf(false) }
  var currentTimeMillis by remember(profileRepository) { mutableStateOf(tabbyCurrentTimeMillis()) }
  var eventState by remember(profileRepository) { mutableStateOf(profilesInitialEventState()) }

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
            val profileName =
              profileUpdateProfileNameText(
                profileName = profileRepository.queryByUuid(action.uuid)?.name,
                unknownText = unknownMessage,
              )
            eventState =
              profileUpdateCompletedEventState(
                getString(ProfileRes.string.toast_profile_updated_complete, profileName)
              )
          }
          .onFailure(onActionError)
      is ProfilesBroadcastAction.ShowUpdateFailed ->
        runCatching {
            val profileName =
              profileUpdateProfileNameText(
                profileName = profileRepository.queryByUuid(action.uuid)?.name,
                unknownText = unknownMessage,
              )
            val displayReason = profileUpdateFailureReasonText(action.reason, unknownMessage)
            eventState =
              profileUpdateFailedEventState(
                message =
                  getString(
                    ProfileRes.string.toast_profile_updated_failed,
                    profileName,
                    displayReason,
                  ),
                uuid = action.uuid,
              )
          }
          .onFailure(onActionError)
    }
  }

  suspend fun consumeEventState() {
    when (val effect = profilesEventRouteEffect(eventState)) {
      ProfilesEventRouteEffect.Ignore -> Unit
      ProfilesEventRouteEffect.OpenCreate -> currentOnCreate.value()
      is ProfilesEventRouteEffect.OpenEdit -> currentOnEdit.value(effect.uuid)
      is ProfilesEventRouteEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
      is ProfilesEventRouteEffect.ShowEditableMessage -> {
        val result =
          snackbarHostState.showSnackbar(
            message = effect.message,
            actionLabel = editActionLabel,
            duration = SnackbarDuration.Long,
          )

        when (profilesEditableSnackbarAction(result.toProfileSnackbarActionResult())) {
          ProfilesEditableSnackbarAction.OpenEdit -> currentOnEdit.value(effect.uuid)
          ProfilesEditableSnackbarAction.Ignore -> Unit
        }
      }
    }

    if (eventState != ProfilesEventState.Idle) {
      eventState = profilesConsumedEventState()
    }
  }

  LaunchedEffect(eventState, editActionLabel) { consumeEventState() }
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
    onCreate = { eventState = profilesOpenCreateEventState() },
    onActivate = { profile ->
      when (profileActivationAction(profile)) {
        ProfileActivationAction.Activate ->
          launchProfileAction { profileRepository.setActive(profile) }
        ProfileActivationAction.RequireSave -> {
          profileActivationEventState(
              action = ProfileActivationAction.RequireSave,
              profileUuid = profile.uuid,
              requireSaveMessage = activeUnsavedTipsMessage,
            )
            ?.let { eventState = it }
        }
      }
    },
    onUpdate = { profile -> launchProfileAction { profileRepository.update(profile.uuid) } },
    onEdit = { profile -> eventState = profilesOpenEditEventState(profile.uuid) },
    onDuplicate = { profile ->
      launchProfileAction {
        val uuid = profileRepository.clone(profile.uuid)

        eventState = profilesOpenEditEventState(uuid)
      }
    },
    onDelete = { profile -> launchProfileAction { profileRepository.delete(profile.uuid) } },
  )
}
