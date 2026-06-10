package com.github.kr328.clash.service

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.service.TabbyStatusProviderCallAction
import com.github.kr328.clash.common.service.tabbyStatusProviderCallAction

class StatusProvider : ContentProvider() {
  override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
    return when (
      val action =
        tabbyStatusProviderCallAction(
          method = method,
          currentProfileMethod = METHOD_CURRENT_PROFILE,
          serviceRunning = serviceRunning,
          currentProfileName = currentProfile,
        )
    ) {
      is TabbyStatusProviderCallAction.CurrentProfile ->
        Bundle().apply { putString("name", action.name) }
      TabbyStatusProviderCallAction.NoResult -> null
      TabbyStatusProviderCallAction.Delegate -> super.call(method, arg, extras)
    }
  }

  override fun insert(uri: Uri, values: ContentValues?): Uri? {
    throw IllegalArgumentException("Stub!")
  }

  override fun query(
    uri: Uri,
    projection: Array<out String>?,
    selection: String?,
    selectionArgs: Array<out String>?,
    sortOrder: String?,
  ): Cursor? {
    throw IllegalArgumentException("Stub!")
  }

  override fun update(
    uri: Uri,
    values: ContentValues?,
    selection: String?,
    selectionArgs: Array<out String>?,
  ): Int {
    throw IllegalArgumentException("Stub!")
  }

  override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
    throw IllegalArgumentException("Stub!")
  }

  override fun getType(uri: Uri): String? {
    throw IllegalArgumentException("Stub!")
  }

  override fun onCreate(): Boolean {
    return true
  }

  companion object {
    const val METHOD_CURRENT_PROFILE = "currentProfile"

    private const val CLASH_SERVICE_RUNNING_FILE = "service_running.lock"

    var serviceRunning: Boolean = false
      set(value) {
        field = value

        shouldStartClashOnBoot = value
      }

    var shouldStartClashOnBoot: Boolean
      get() = Global.application.filesDir.resolve(CLASH_SERVICE_RUNNING_FILE).exists()
      set(value) {
        Global.application.filesDir.resolve(CLASH_SERVICE_RUNNING_FILE).apply {
          if (value) createNewFile() else delete()
        }
      }

    var currentProfile: String? = null
  }
}
