package com.github.kr328.clash.home.ui

import android.app.Activity
import androidx.activity.result.ActivityResult

internal fun ActivityResult.toHomeVpnPermissionResult(): HomeVpnPermissionResult {
  return if (resultCode == Activity.RESULT_OK) {
    HomeVpnPermissionResult.Granted
  } else {
    HomeVpnPermissionResult.Denied
  }
}
