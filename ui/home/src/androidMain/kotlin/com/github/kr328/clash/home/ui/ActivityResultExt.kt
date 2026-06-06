package com.github.kr328.clash.home.ui

import android.app.Activity
import androidx.activity.result.ActivityResult

internal fun ActivityResult.toHomeVpnPermissionResult(): HomeVpnPermissionResult {
  return homeVpnPermissionResultFromPlatformResultCode(
    resultCode = resultCode,
    grantedResultCode = Activity.RESULT_OK,
  )
}
