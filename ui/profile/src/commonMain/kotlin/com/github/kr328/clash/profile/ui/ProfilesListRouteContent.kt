package com.github.kr328.clash.profile.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.Profile
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tabby.ui.profile.generated.resources.Res as ProfileRes
import tabby.ui.profile.generated.resources.format_type_unsaved
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.external
import tabby.ui.shared.generated.resources.file
import tabby.ui.shared.generated.resources.url

@Composable
fun ProfilesListRouteContent(
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  profiles: List<Profile> = emptyList(),
  allUpdating: Boolean = false,
  currentTimeMillis: Long = 0,
  formatBytes: (Long) -> String = ::profileBinaryBytesText,
  formatExpire: (Long) -> String = ::profileExpireDateString,
  formatElapsedMillis: @Composable (Long) -> String = { elapsedTimeTextString(it) },
  onUpdateAll: () -> Unit = {},
  onCreate: () -> Unit = {},
  onActivate: (Profile) -> Unit = {},
  onUpdate: (Profile) -> Unit = {},
  onEdit: (Profile) -> Unit = {},
  onDuplicate: (Profile) -> Unit = {},
  onDelete: (Profile) -> Unit = {},
) {
  val effectiveCurrentTime =
    maxOf(currentTimeMillis, profiles.maxOfOrNull { it.updatedAt } ?: currentTimeMillis)

  ProfilesContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    profiles =
      profiles.toProfileRouteListItems(
        currentTimeMillis = effectiveCurrentTime,
        formatBytes = formatBytes,
        formatExpire = formatExpire,
        formatElapsedMillis = formatElapsedMillis,
      ),
    allUpdating = allUpdating,
    hasUpdatableProfile = hasUpdatableProfiles(profiles),
    onUpdateAll = onUpdateAll,
    onCreate = onCreate,
    onActivate = onActivate,
    onUpdate = onUpdate,
    onEdit = onEdit,
    onDuplicate = onDuplicate,
    onDelete = onDelete,
  )
}

@Composable
private fun List<Profile>.toProfileRouteListItems(
  currentTimeMillis: Long,
  formatBytes: (Long) -> String,
  formatExpire: (Long) -> String,
  formatElapsedMillis: @Composable (Long) -> String,
): List<ProfileListItem> {
  val items = ArrayList<ProfileListItem>(size)

  for (profile in this) {
    val usedTraffic = profile.download + profile.upload
    val showTraffic = profile.showsTrafficUsage()

    items +=
      ProfileListItem(
        profile = profile,
        typeText =
          profileTypeTextString(profileTypeText(type = profile.type, pending = profile.pending)),
        usageText =
          if (showTraffic) {
            "${formatBytes(usedTraffic)} / ${formatBytes(profile.total)}"
          } else {
            null
          },
        expireText = profile.expire.takeIf { it != 0L }?.let(formatExpire),
        updatedAtText = formatElapsedMillis(currentTimeMillis - profile.updatedAt),
        trafficProgress = if (showTraffic) profile.trafficProgress(usedTraffic) else 0,
      )
  }

  return items
}

@Composable
private fun profileTypeTextString(typeText: ProfileTypeText): String {
  val text =
    stringResource(
      profileTypeTextResourceToken(
        token = typeText.token,
        file = SharedRes.string.file,
        url = SharedRes.string.url,
        external = SharedRes.string.external,
      )
    )

  return if (typeText.pending) {
    stringResource(ProfileRes.string.format_type_unsaved, text)
  } else {
    text
  }
}

internal fun profileExpireDateString(epochMillis: Long): String {
  val date =
    Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault()).date

  return "${date.year}-${date.month.number.twoDigitString()}-${date.day.twoDigitString()}"
}

private fun Int.twoDigitString(): String {
  return if (this < 10) "0$this" else toString()
}
