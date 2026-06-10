package com.github.kr328.clash.common.compat

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build

fun PackageManager.getInstalledPackagesCompat(flags: Int): List<PackageInfo> {
  return if (tabbyPackageManagerUsesTypedFlags(Build.VERSION.SDK_INT)) {
    getInstalledPackages(PackageManager.PackageInfoFlags.of(flags.toLong()))
  } else {
    getInstalledPackages(flags)
  }
}

fun PackageManager.queryIntentActivitiesCompat(intent: Intent, flags: Int): List<ResolveInfo> {
  return if (tabbyPackageManagerUsesTypedFlags(Build.VERSION.SDK_INT)) {
    queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
  } else {
    queryIntentActivities(intent, flags)
  }
}
