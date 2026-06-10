package com.github.kr328.clash.profile.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.core.model.isHttpProfileSource
import com.github.kr328.clash.core.model.profileAutoUpdateIntervalMillisFromMinutesInput
import com.github.kr328.clash.ui.component.ModelProgressBarDialog
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineSave
import com.github.kr328.clash.ui.icon.OutlineFolder
import com.github.kr328.clash.ui.icon.OutlineInbox
import com.github.kr328.clash.ui.icon.OutlineInfo
import com.github.kr328.clash.ui.icon.OutlineLabel
import com.github.kr328.clash.ui.icon.OutlineUpdate
import com.github.kr328.clash.ui.icon.TabbyIcons
import kotlin.time.Duration.Companion.milliseconds
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.TextFieldPreference
import me.zhanghai.compose.preference.preference
import org.jetbrains.compose.resources.stringResource
import tabby.ui.profile.generated.resources.Res as ProfileRes
import tabby.ui.profile.generated.resources.accept_http_content
import tabby.ui.profile.generated.resources.auto_update
import tabby.ui.profile.generated.resources.browse_configuration_providers
import tabby.ui.profile.generated.resources.browse_files
import tabby.ui.profile.generated.resources.exit_without_save
import tabby.ui.profile.generated.resources.exit_without_save_warning
import tabby.ui.profile.generated.resources.format_minutes
import tabby.ui.profile.generated.resources.profile_name
import tabby.ui.profile.generated.resources.properties
import tabby.ui.profile.generated.resources.save
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.cancel
import tabby.ui.shared.generated.resources.disabled
import tabby.ui.shared.generated.resources.name
import tabby.ui.shared.generated.resources.ok
import tabby.ui.shared.generated.resources.url

internal data class PropertiesProgressState(
  val visible: Boolean = false,
  val isIndeterminate: Boolean = false,
  val text: String? = null,
  val progress: Int = 0,
  val max: Int = 0,
)

@Composable
internal fun PropertiesContent(
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState,
  profile: Profile,
  processing: Boolean,
  progressState: PropertiesProgressState,
  showExitWithoutSavingDialog: Boolean,
  tipsProperties: AnnotatedString,
  onBack: () -> Unit,
  onDismissExitWithoutSavingDialog: () -> Unit,
  onBrowseFiles: () -> Unit,
  onCommit: () -> Unit,
  onRequestClose: () -> Unit,
  onNameChanged: (String) -> Unit,
  onUrlChanged: (String) -> Unit,
  onIntervalChanged: (Long) -> Unit,
) {
  TabbyScaffold(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    title = stringResource(ProfileRes.string.properties),
    onBack = onBack,
    actions = {
      if (processing) {
        CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.5.dp)
      } else {
        IconButton(onClick = onCommit) {
          Icon(
            imageVector = TabbyIcons.BaselineSave,
            contentDescription = stringResource(ProfileRes.string.save),
          )
        }
      }
    },
  ) { innerPadding ->
    ProvidePreferenceLocals {
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        preference(
          key = "tips",
          title = { Text(stringResource(ProfileRes.string.properties)) },
          summary = { Text(tipsProperties) },
          icon = { Icon(imageVector = TabbyIcons.OutlineInfo, contentDescription = null) },
        )
        item(key = "name") {
          TextFieldPreference<String?>(
            value = profile.name,
            onValueChange = { newName ->
              if (newName != null && newName != profile.name) {
                onNameChanged(newName)
              }
            },
            title = { Text(stringResource(SharedRes.string.name)) },
            textToValue = { input -> input.takeIf(::isNotBlank) },
            icon = { Icon(imageVector = TabbyIcons.OutlineLabel, contentDescription = null) },
            summary = {
              Text(profile.name.ifBlank { stringResource(ProfileRes.string.profile_name) })
            },
          )
        }
        item(key = "source") {
          TextFieldPreference<String?>(
            value = profile.source,
            onValueChange = { newUrl ->
              if (newUrl != null && newUrl != profile.source) {
                onUrlChanged(newUrl)
              }
            },
            title = { Text(stringResource(SharedRes.string.url)) },
            textToValue = ::profilePropertiesSourceInputValue,
            enabled = profile.type != Profile.Type.File && profile.type != Profile.Type.External,
            icon = { Icon(imageVector = TabbyIcons.OutlineInbox, contentDescription = null) },
            summary = {
              Text(profile.source.ifBlank { stringResource(ProfileRes.string.accept_http_content) })
            },
          )
        }
        item(key = "interval") {
          TextFieldPreference<Long?>(
            value = profile.interval,
            onValueChange = { interval ->
              if (interval != null && interval != profile.interval) {
                onIntervalChanged(interval)
              }
            },
            title = { Text(stringResource(ProfileRes.string.auto_update)) },
            textToValue = ::profileAutoUpdateIntervalMillisFromMinutesInput,
            enabled = profile.type != Profile.Type.File,
            icon = { Icon(imageVector = TabbyIcons.OutlineUpdate, contentDescription = null) },
            summary = {
              val intervalSummary =
                if (profile.interval == 0L) {
                  stringResource(SharedRes.string.disabled)
                } else {
                  stringResource(
                    ProfileRes.string.format_minutes,
                    profile.interval.milliseconds.inWholeMinutes,
                  )
                }
              Text(intervalSummary)
            },
            valueToText = { interval ->
              if (interval == null || interval == 0L) {
                ""
              } else {
                interval.milliseconds.inWholeMinutes.toString()
              }
            },
          )
        }
        preference(
          key = "browse_files",
          title = { Text(stringResource(ProfileRes.string.browse_files)) },
          summary = { Text(stringResource(ProfileRes.string.browse_configuration_providers)) },
          icon = { Icon(imageVector = TabbyIcons.OutlineFolder, contentDescription = null) },
          onClick = onBrowseFiles,
        )
      }
    }
  }

  if (showExitWithoutSavingDialog) {
    ExitWithoutSavingDialog(
      onConfirm = onRequestClose,
      onDismiss = onDismissExitWithoutSavingDialog,
    )
  }

  ModelProgressBarDialog(
    visible = progressState.visible,
    isIndeterminate = progressState.isIndeterminate,
    text = progressState.text,
    progress = progressState.progress,
    max = progressState.max,
  )
}

@Composable
private fun ExitWithoutSavingDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(text = stringResource(ProfileRes.string.exit_without_save)) },
    text = { Text(text = stringResource(ProfileRes.string.exit_without_save_warning)) },
    confirmButton = {
      TextButton(onClick = onConfirm) { Text(text = stringResource(SharedRes.string.ok)) }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(text = stringResource(SharedRes.string.cancel)) }
    },
  )
}

private fun isNotBlank(value: String): Boolean = value.isNotBlank()

internal fun profilePropertiesSourceInputValue(value: String): String? =
  value.takeIf(::isHttpProfileSource)
