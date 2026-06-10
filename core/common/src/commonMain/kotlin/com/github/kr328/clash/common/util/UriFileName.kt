package com.github.kr328.clash.common.util

fun tabbyFileNameFromSchemeSpecificPart(schemeSpecificPart: String?): String? {
  return schemeSpecificPart?.split("/")?.lastOrNull()
}
