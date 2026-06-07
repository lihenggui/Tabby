package com.github.kr328.clash.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.engine.api.ProfileRepository
import com.github.kr328.clash.profile.ui.PropertiesRouteContent
import kotlin.uuid.Uuid
import kotlinx.coroutines.awaitCancellation

@Composable
internal fun ProfileRepositoryPropertiesRouteContent(
  profileRepository: ProfileRepository,
  uuid: Uuid,
  onBrowseFiles: (Uuid) -> Unit,
  onFinish: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  onActionError: (Throwable) -> Unit = {},
) {
  var profile by remember(profileRepository, uuid) { mutableStateOf<Profile?>(null) }

  LaunchedEffect(profileRepository, uuid) {
    try {
      val loadedProfile =
        runCatching { profileRepository.queryByUuid(uuid) }.onFailure(onActionError).getOrNull()
      if (loadedProfile == null) {
        onFinish(false)
        return@LaunchedEffect
      }

      profile = loadedProfile
      awaitCancellation()
    } finally {
      runCatching { profileRepository.release(uuid) }.onFailure(onActionError)
    }
  }

  profile?.let { loadedProfile ->
    PropertiesRouteContent(
      modifier = modifier,
      profile = loadedProfile,
      onBrowseFiles = { onBrowseFiles(it.uuid) },
      onCommit = { committedProfile ->
        profileRepository.patch(
          uuid = committedProfile.uuid,
          name = committedProfile.name,
          source = committedProfile.source,
          interval = committedProfile.interval,
        )
        profileRepository.commit(committedProfile.uuid)
      },
      onFinish = onFinish,
    )
  }
}
