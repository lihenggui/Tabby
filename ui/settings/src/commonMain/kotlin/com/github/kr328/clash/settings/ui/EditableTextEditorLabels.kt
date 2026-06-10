package com.github.kr328.clash.settings.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tabby.ui.settings.generated.resources.Res
import tabby.ui.settings.generated.resources._new
import tabby.ui.settings.generated.resources.cancel
import tabby.ui.settings.generated.resources.delete
import tabby.ui.settings.generated.resources.key
import tabby.ui.settings.generated.resources.ok
import tabby.ui.settings.generated.resources.reorder
import tabby.ui.settings.generated.resources.reset
import tabby.ui.settings.generated.resources.value

internal data class EditableTextEditorLabels(
  val add: String,
  val delete: String,
  val reorder: String,
  val reset: String,
  val cancel: String,
  val ok: String,
  val key: String,
  val value: String,
)

@Composable
internal fun editableTextEditorLabels() =
  EditableTextEditorLabels(
    add = stringResource(Res.string._new),
    delete = stringResource(Res.string.delete),
    reorder = stringResource(Res.string.reorder),
    reset = stringResource(Res.string.reset),
    cancel = stringResource(Res.string.cancel),
    ok = stringResource(Res.string.ok),
    key = stringResource(Res.string.key),
    value = stringResource(Res.string.value),
  )
