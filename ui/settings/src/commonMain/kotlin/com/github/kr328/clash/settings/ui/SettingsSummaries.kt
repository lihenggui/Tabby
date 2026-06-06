package com.github.kr328.clash.settings.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tabby.ui.settings.generated.resources.Res
import tabby.ui.settings.generated.resources.empty
import tabby.ui.settings.generated.resources.format_elements

@Composable
internal fun Collection<*>?.listSummary(placeholder: String): String =
  itemCountSummary(size = this?.size, placeholder = placeholder)

@Composable
internal fun Map<*, *>?.mapSummary(placeholder: String): String =
  itemCountSummary(size = this?.size, placeholder = placeholder)

@Composable
private fun itemCountSummary(size: Int?, placeholder: String): String =
  when {
    size == null -> placeholder
    size == 0 -> stringResource(Res.string.empty)
    else -> stringResource(Res.string.format_elements, size)
  }
