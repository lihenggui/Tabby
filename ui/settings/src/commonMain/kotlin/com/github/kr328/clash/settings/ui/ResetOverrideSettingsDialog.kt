package com.github.kr328.clash.settings.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tabby.ui.settings.generated.resources.Res
import tabby.ui.settings.generated.resources.cancel
import tabby.ui.settings.generated.resources.ok
import tabby.ui.settings.generated.resources.reset_override_settings
import tabby.ui.settings.generated.resources.reset_override_settings_message

@Composable
internal fun ResetOverrideSettingsDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(Res.string.reset_override_settings)) },
    text = { Text(stringResource(Res.string.reset_override_settings_message)) },
    confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(Res.string.ok)) } },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) } },
  )
}
