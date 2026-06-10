package com.github.kr328.clash.common.util

val PatternFileName = "[^*&%\\n\\r/]+".toRegex()

fun tabbyIsValidFileNameInput(value: String): Boolean {
  return value.isNotBlank() && PatternFileName.matches(value)
}
