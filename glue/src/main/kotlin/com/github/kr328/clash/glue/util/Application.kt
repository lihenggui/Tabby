package com.github.kr328.clash.glue.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import com.github.kr328.clash.common.app.tabbyApkHasSupportedClashNativeLibrary
import com.github.kr328.clash.common.log.Log
import java.io.File
import java.util.zip.ZipFile

object ApplicationObserver {
  private val _visibleActivities: MutableSet<Activity> = mutableSetOf()

  private var visibleChanged: (Boolean) -> Unit = {}

  private var appVisible = false
    set(value) {
      if (field != value) {
        field = value

        visibleChanged(value)
      }
    }

  val createdActivities: Set<Activity>
    field: MutableSet<Activity> = mutableSetOf()

  private val activityObserver =
    object : Application.ActivityLifecycleCallbacks {
      @Synchronized
      override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        createdActivities.add(activity)
      }

      @Synchronized
      override fun onActivityDestroyed(activity: Activity) {
        createdActivities.remove(activity)
        _visibleActivities.remove(activity)
        appVisible = _visibleActivities.isNotEmpty()
      }

      override fun onActivityStarted(activity: Activity) {
        _visibleActivities.add(activity)
        appVisible = true
      }

      override fun onActivityStopped(activity: Activity) {
        _visibleActivities.remove(activity)
        appVisible = _visibleActivities.isNotEmpty()
      }

      override fun onActivityPaused(activity: Activity) {}

      override fun onActivityResumed(activity: Activity) {}

      override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    }

  fun onVisibleChanged(visibleChanged: (Boolean) -> Unit) {
    this.visibleChanged = visibleChanged
  }

  fun attach(application: Application) {
    application.registerActivityLifecycleCallbacks(activityObserver)
  }
}

fun Context.verifyApk(): Boolean {
  return try {
    val info = applicationInfo
    val sources = info.splitSourceDirs ?: arrayOf(info.sourceDir) ?: return false

    val availableAbi = Build.SUPPORTED_ABIS.toSet()
    val apkEntryNames =
      sources
        .asSequence()
        .filter { File(it).exists() }
        .flatMap { ZipFile(it).entries().asSequence() }
        .map { it.name }

    tabbyApkHasSupportedClashNativeLibrary(
      supportedAbis = availableAbi,
      apkEntryNames = apkEntryNames,
    )
  } catch (e: Exception) {
    Log.e("Verify apk failed: ${e.message}", e)
    false
  }
}
