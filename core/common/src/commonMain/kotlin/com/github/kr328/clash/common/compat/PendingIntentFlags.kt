package com.github.kr328.clash.common.compat

const val TABBY_PENDING_INTENT_MUTABLE_MIN_SDK = 31

fun tabbyPendingIntentFlagsFromPlatformState(
  flags: Int,
  mutable: Boolean,
  platformSdk: Int,
  mutableFlag: Int,
  immutableFlag: Int,
): Int {
  return if (platformSdk >= TABBY_PENDING_INTENT_MUTABLE_MIN_SDK && mutable) {
    flags or mutableFlag
  } else {
    flags or immutableFlag
  }
}
