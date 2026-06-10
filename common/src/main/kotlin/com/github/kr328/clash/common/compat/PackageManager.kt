package com.github.kr328.clash.common.compat

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

fun PackageManager.getInstalledPackagesCompat(flags: Int): List<PackageInfo> {
  return if (usesTypedPackageManagerFlags()) {
    getInstalledPackages(PackageManager.PackageInfoFlags.of(flags.toLong()))
  } else {
    getInstalledPackages(flags)
  }
}

fun PackageManager.queryIntentActivitiesCompat(intent: Intent, flags: Int): List<ResolveInfo> {
  return if (usesTypedPackageManagerFlags()) {
    queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
  } else {
    queryIntentActivities(intent, flags)
  }
}

@ChecksSdkIntAtLeast(api = TABBY_PACKAGE_MANAGER_TYPED_FLAGS_MIN_SDK)
private fun usesTypedPackageManagerFlags(): Boolean {
  return tabbyPackageManagerUsesTypedFlags(Build.VERSION.SDK_INT)
}
