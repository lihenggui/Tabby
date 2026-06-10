package com.github.kr328.clash.common.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import com.github.kr328.clash.common.app.TabbyLauncherActivitySpec
import com.github.kr328.clash.common.app.tabbyMainActivityAliasFromLauncherActivities
import com.github.kr328.clash.common.compat.TABBY_SERIALIZABLE_EXTRA_TYPED_API_MIN_SDK
import com.github.kr328.clash.common.compat.queryIntentActivitiesCompat
import com.github.kr328.clash.common.compat.tabbySerializableExtraUsesTypedApi
import com.github.kr328.clash.common.compat.tabbyUriPermissionGrantFlags
import com.github.kr328.clash.common.di.AppInfoProvider.Companion.instance as appInfoProvider
import java.io.Serializable
import kotlin.uuid.Uuid

fun Context.mainIntent(block: Intent.() -> Unit = {}): Intent {
  val mainActivityClass = appInfoProvider.mainActivityClass
  return Intent(this, mainActivityClass).apply(block = block)
}

val Context.mainActivityAlias: ComponentName
  get() {
    val mainActivityName = appInfoProvider.mainActivityClass.name
    val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val resolveFlags = PackageManager.MATCH_DISABLED_COMPONENTS

    return packageManager
      .queryIntentActivitiesCompat(launcherIntent, resolveFlags)
      .asSequence()
      .map { resolveInfo ->
        val activityInfo = resolveInfo.activityInfo
        TabbyLauncherActivitySpec(
          packageName = activityInfo.packageName,
          name = activityInfo.name,
          targetActivity = activityInfo.targetActivity,
        )
      }
      .let { tabbyMainActivityAliasFromLauncherActivities(mainActivityName, it) }
      ?.let { ComponentName(it.packageName, it.name) }
      ?: error("Launcher alias targeting $mainActivityName is not declared in AndroidManifest.xml")
  }

fun Intent.grantPermissions(read: Boolean = true, write: Boolean = true): Intent = apply {
  addFlags(
    tabbyUriPermissionGrantFlags(
      read = read,
      write = write,
      readFlag = Intent.FLAG_GRANT_READ_URI_PERMISSION,
      writeFlag = Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
    )
  )
}

var Intent.uuid: Uuid?
  get() =
    tabbyUuidFromUriPayload(
      scheme = data?.scheme,
      schemeSpecificPart = data?.schemeSpecificPart,
    )
  set(value) {
    data =
      if (value == null) {
        null
      } else {
        Uri.fromParts(TABBY_UUID_URI_SCHEME, tabbyUuidUriSchemeSpecificPart(value), null)
      }
  }

fun Intent.setUUID(uuid: Uuid): Intent = apply { this.uuid = uuid }

inline fun <reified T : Serializable> Intent.getSerializableCompat(key: String): T? =
  when {
    usesTypedSerializableExtraApi() -> getSerializableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
  }

@PublishedApi
@ChecksSdkIntAtLeast(api = TABBY_SERIALIZABLE_EXTRA_TYPED_API_MIN_SDK)
internal fun usesTypedSerializableExtraApi(): Boolean {
  return tabbySerializableExtraUsesTypedApi(Build.VERSION.SDK_INT)
}
