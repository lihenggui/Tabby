package com.github.kr328.clash.profile.ui

internal fun decodeProfileQrSource(rawValue: String?, rawBytes: ByteArray?): String {
  return rawValue ?: rawBytes?.decodeToString().orEmpty()
}
