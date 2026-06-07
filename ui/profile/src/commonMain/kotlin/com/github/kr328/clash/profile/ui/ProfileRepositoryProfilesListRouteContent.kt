package com.github.kr328.clash.profile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.kr328.clash.engine.api.ProfileRepository
import kotlin.uuid.Uuid
import kotlinx.coroutines.launch

@Composable
fun ProfileRepositoryProfilesListRouteContent(
  profileRepository: ProfileRepository,
  onCreate: () -> Unit,
  onEdit: (Uuid) -> Unit,
  modifier: Modifier = Modifier,
  onActionError: (Throwable) -> Unit = {},
) {
  val profiles by profileRepository.observeProfiles().collectAsState(initial = emptyList())
  val scope = rememberCoroutineScope()
  var allUpdating by remember { mutableStateOf(false) }

  fun launchProfileAction(block: suspend () -> Unit) {
    scope.launch { runCatching { block() }.onFailure(onActionError) }
  }

  ProfilesListRouteContent(
    modifier = modifier,
    profiles = profiles,
    allUpdating = allUpdating,
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
        onEdit(profile.uuid)
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
