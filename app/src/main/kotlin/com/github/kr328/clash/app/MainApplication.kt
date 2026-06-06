package com.github.kr328.clash.app

import android.app.Application
import android.content.Context
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.util.clashDir
import com.github.kr328.clash.service.util.sendServiceRecreated
import java.io.File
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {

  override fun attachBaseContext(base: Context?) {
    super.attachBaseContext(base)

    Global.init(this)
  }

  override fun onCreate() {
    super.onCreate()

    koin()

    val processName = getProcessName()
    extractGeoFiles()

    Log.d("Process $processName started")

    when (tabbyProcessStartupAction(processName, packageName)) {
      TabbyProcessStartupAction.StartMainProcess -> Remote.launch()
      TabbyProcessStartupAction.NotifyServiceRecreated -> sendServiceRecreated()
    }
  }

  private fun koin() {
    startKoin {
      androidLogger()
      androidContext(this@MainApplication)
      modules(appModule)
    }
  }

  private fun extractGeoFiles() {
    clashDir.mkdirs()

    val updateDate = packageManager.getPackageInfo(packageName, 0).lastUpdateTime
    tabbyGeoAssets().forEach { asset ->
      val geoFile = File(clashDir, asset.outputName)
      when (
        tabbyGeoFileUpdateAction(
          fileExists = geoFile.exists(),
          fileLastModifiedMillis = geoFile.lastModified(),
          packageLastUpdateMillis = updateDate,
        )
      ) {
        TabbyGeoFileUpdateAction.DeleteStaleFile -> {
          geoFile.delete()
          when (tabbyGeoFileDeletedAction(fileExistsAfterDelete = geoFile.exists())) {
            TabbyGeoFileUpdateAction.ExtractMissingFile -> extractGeoFile(asset, geoFile)
            TabbyGeoFileUpdateAction.DeleteStaleFile,
            TabbyGeoFileUpdateAction.Ignore -> Unit
          }
        }
        TabbyGeoFileUpdateAction.ExtractMissingFile -> extractGeoFile(asset, geoFile)
        TabbyGeoFileUpdateAction.Ignore -> Unit
      }
    }
  }

  private fun extractGeoFile(asset: TabbyGeoAsset, geoFile: File) {
    geoFile.outputStream().use { assets.open(asset.assetName).copyTo(it) }
  }

  fun finalize() {
    Global.destroy()
  }
}
