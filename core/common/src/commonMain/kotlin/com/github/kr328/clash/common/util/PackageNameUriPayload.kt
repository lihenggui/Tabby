package com.github.kr328.clash.common.util

const val TABBY_PACKAGE_NAME_URI_SCHEME = "package"

fun tabbyPackageNameFromUriPayload(scheme: String?, schemeSpecificPart: String?): String? {
  if (scheme != TABBY_PACKAGE_NAME_URI_SCHEME) {
    return null
  }

  return schemeSpecificPart
}
