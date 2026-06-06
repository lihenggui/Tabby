package com.github.kr328.clash.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.kr328.clash.glue.util.startClashService
import com.github.kr328.clash.service.StatusProvider

class RestartReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    when (
      tabbyRestartReceiverAction(
        event = intent.tabbyRestartReceiverEvent(),
        shouldStartClashOnBoot = StatusProvider.shouldStartClashOnBoot,
      )
    ) {
      TabbyRestartReceiverAction.StartClash -> context.startClashService()
      TabbyRestartReceiverAction.Ignore -> Unit
    }
  }
}

private fun Intent.tabbyRestartReceiverEvent(): TabbyRestartReceiverEvent? =
  when (action) {
    Intent.ACTION_BOOT_COMPLETED -> TabbyRestartReceiverEvent.BootCompleted
    Intent.ACTION_MY_PACKAGE_REPLACED -> TabbyRestartReceiverEvent.PackageReplaced
    else -> null
  }
