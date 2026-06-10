package me.zhanghai.compose.preference

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString

internal fun <T> LazyListScope.listPreference(
  key: String,
  value: T,
  onValueChange: (T) -> Unit,
  values: List<T>,
  title: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  icon: @Composable (() -> Unit)? = null,
  summary: @Composable (() -> Unit)? = null,
  type: ListPreferenceType = ListPreferenceType.ALERT_DIALOG,
  valueToText: @Composable (T) -> AnnotatedString = { AnnotatedString(it.toString()) },
) {
  item(key = key, contentType = "ListPreference") {
    ListPreference(
      value = value,
      onValueChange = onValueChange,
      values = values,
      title = title,
      modifier = modifier,
      enabled = enabled,
      icon = icon,
      summary = summary,
      type = type,
      valueToText = valueToText,
    )
  }
}

internal fun LazyListScope.switchPreference(
  key: String,
  value: Boolean,
  onValueChange: (Boolean) -> Unit,
  title: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  icon: @Composable (() -> Unit)? = null,
  summary: @Composable (() -> Unit)? = null,
) {
  item(key = key, contentType = "SwitchPreference") {
    SwitchPreference(
      value = value,
      onValueChange = onValueChange,
      title = title,
      modifier = modifier,
      enabled = enabled,
      icon = icon,
      summary = summary,
    )
  }
}
