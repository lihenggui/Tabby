package com.github.kr328.clash.common.compat

import android.app.PendingIntent
import android.os.Build

fun pendingIntentFlags(flags: Int, mutable: Boolean = false): Int {
  return tabbyPendingIntentFlagsFromPlatformState(
    flags = flags,
    mutable = mutable,
    platformSdk = Build.VERSION.SDK_INT,
    mutableFlag = PendingIntent.FLAG_MUTABLE,
    immutableFlag = PendingIntent.FLAG_IMMUTABLE,
  )
}
