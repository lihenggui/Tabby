package com.github.kr328.clash.settings.ui

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

internal fun initialTextFieldValue(text: String) =
  TextFieldValue(text = text, selection = TextRange(text.length))

internal fun TextFieldValue.filterDigits(): TextFieldValue {
  val filtered = text.filter(Char::isDigit)
  if (filtered == text) return this
  val start = text.take(selection.start).count(Char::isDigit)
  val end = text.take(selection.end).count(Char::isDigit)
  return copy(text = filtered, selection = TextRange(start, end))
}
