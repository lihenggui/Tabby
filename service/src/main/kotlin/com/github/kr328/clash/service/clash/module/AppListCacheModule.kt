package com.github.kr328.clash.service.clash.module

import android.app.Service
import android.content.Intent
import com.github.kr328.clash.common.app.tabbyInstalledAppCacheEntries
import com.github.kr328.clash.common.app.tabbyInstalledAppInfoFromPlatformFields
import com.github.kr328.clash.common.compat.getInstalledPackagesCompat
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.Clash
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay

class AppListCacheModule(service: Service) : Module<Unit>(service) {
  private fun reload() {
    val packages =
      tabbyInstalledAppCacheEntries(
        service.packageManager.getInstalledPackagesCompat(0).mapNotNull {
          tabbyInstalledAppInfoFromPlatformFields(
            uid = it.applicationInfo?.uid,
            packageName = it.packageName,
            sharedUserId = it.sharedUserId,
          )
        }
      )

    Clash.notifyInstalledAppsChanged(packages)

    Log.d("Installed ${packages.size} packages cached")
  }

  override suspend fun run() {
    val packageChanged =
      receiveBroadcast(false, Channel.CONFLATED) {
        addAction(Intent.ACTION_PACKAGE_ADDED)
        addAction(Intent.ACTION_PACKAGE_REMOVED)
        addDataScheme("package")
      }

    while (true) {
      reload()

      packageChanged.receive()

      delay(10.seconds)
    }
  }
}
