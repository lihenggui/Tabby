package com.github.kr328.clash.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.kr328.clash.common.util.mainIntent

class DialerReceiver : BroadcastReceiver() {
  @Suppress("UnsafeProtectedBroadcastReceiver")
  override fun onReceive(context: Context, intent: Intent) {
    when (tabbyDialerReceiverAction()) {
      TabbyDialerReceiverAction.OpenMainActivity ->
        context.startActivity(context.mainIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
  }
}
