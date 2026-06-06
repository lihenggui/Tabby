package com.github.kr328.clash.profile.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.Profile
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.milliseconds
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
import tabby.ui.shared.generated.resources.format_days_ago
import tabby.ui.shared.generated.resources.format_hours_ago
import tabby.ui.shared.generated.resources.format_minutes_ago
import tabby.ui.shared.generated.resources.recently
import tabby.ui.shared.generated.resources.url

@Composable
fun ProfilesListRouteContent(
  modifier: Modifier = Modifier,
  profiles: List<Profile> = emptyList(),
  allUpdating: Boolean = false,
  currentTimeMillis: Long = 0,
  formatBytes: (Long) -> String = ::profileBinaryBytesString,
  formatExpire: (Long) -> String = ::profileExpireDateString,
  onUpdateAll: () -> Unit = {},
  onCreate: () -> Unit = {},
  onActivate: (Profile) -> Unit = {},
  onUpdate: (Profile) -> Unit = {},
  onEdit: (Profile) -> Unit = {},
  onDuplicate: (Profile) -> Unit = {},
  onDelete: (Profile) -> Unit = {},
) {
  val snackbarHostState = remember { SnackbarHostState() }
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
        updatedAtText = profileElapsedMillisString(currentTimeMillis - profile.updatedAt),
        trafficProgress = if (showTraffic) profile.trafficProgress(usedTraffic) else 0,
      )
  }

  return items
}

@Composable
private fun profileTypeTextString(typeText: ProfileTypeText): String {
  val text =
    when (typeText.token) {
      ProfileTypeTextToken.File -> stringResource(SharedRes.string.file)
      ProfileTypeTextToken.Url -> stringResource(SharedRes.string.url)
      ProfileTypeTextToken.External -> stringResource(SharedRes.string.external)
    }

  return if (typeText.pending) {
    stringResource(ProfileRes.string.format_type_unsaved, text)
  } else {
    text
  }
}

@Composable
private fun profileElapsedMillisString(elapsedMillis: Long): String {
  val duration = elapsedMillis.coerceAtLeast(0).milliseconds
  val day = duration.inWholeDays
  val hour = duration.inWholeHours
  val minute = duration.inWholeMinutes

  return when {
    day > 0 -> stringResource(SharedRes.string.format_days_ago, day)
    hour > 0 -> stringResource(SharedRes.string.format_hours_ago, hour)
    minute > 0 -> stringResource(SharedRes.string.format_minutes_ago, minute)
    else -> stringResource(SharedRes.string.recently)
  }
}

private fun profileBinaryBytesString(bytes: Long): String {
  val units = arrayOf("B", "KiB", "MiB", "GiB", "TiB", "PiB")
  var value = bytes.coerceAtLeast(0).toDouble()
  var unitIndex = 0

  while (value >= BINARY_UNIT_SIZE && unitIndex < units.lastIndex) {
    value /= BINARY_UNIT_SIZE
    unitIndex += 1
  }

  return if (unitIndex == 0) {
    "${value.toLong()} ${units[unitIndex]}"
  } else {
    "${value.oneDecimalString()} ${units[unitIndex]}"
  }
}

private fun profileExpireDateString(epochMillis: Long): String {
  val date =
    Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault()).date

  return "${date.year}-${date.month.number.twoDigitString()}-${date.day.twoDigitString()}"
}

private fun Double.oneDecimalString(): String {
  val rounded = (this * 10.0).roundToLong()
  val whole = rounded / 10L
  val fraction = rounded % 10L

  return if (fraction == 0L) {
    whole.toString()
  } else {
    "$whole.$fraction"
  }
}

private fun Int.twoDigitString(): String {
  return if (this < 10) "0$this" else toString()
}

private fun Profile.showsTrafficUsage(): Boolean {
  return download >= MIN_DOWNLOAD_BYTES_FOR_USAGE && total > MIN_TOTAL_BYTES_FOR_USAGE
}

private fun Profile.trafficProgress(usedTraffic: Long): Int {
  return (usedTraffic.toDouble() / total.toDouble() * TRAFFIC_PROGRESS_SCALE)
    .toInt()
    .coerceIn(0, TRAFFIC_PROGRESS_SCALE)
}

private const val BINARY_UNIT_SIZE = 1024.0
private const val MIN_DOWNLOAD_BYTES_FOR_USAGE = 2L
private const val MIN_TOTAL_BYTES_FOR_USAGE = 1L
private const val TRAFFIC_PROGRESS_SCALE = 1000
