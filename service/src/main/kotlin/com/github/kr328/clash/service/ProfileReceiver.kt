package com.github.kr328.clash.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.compat.pendingIntentFlags
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.util.componentName
import com.github.kr328.clash.common.util.setUUID
import com.github.kr328.clash.core.model.ProfileReceiverStartAction
import com.github.kr328.clash.core.model.profileAutoUpdateScheduleDelayMillis
import com.github.kr328.clash.core.model.profileReceiverStartAction
import com.github.kr328.clash.core.model.profileSupportsAutoUpdateSchedule
import com.github.kr328.clash.service.data.Imported
import com.github.kr328.clash.service.data.ImportedDao
import com.github.kr328.clash.service.util.importedDir
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ProfileReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    when (
      profileReceiverStartAction(
        action = intent.action,
        scheduleTriggerActions = PROFILE_SCHEDULE_TRIGGER_ACTIONS,
        requestUpdateAction = Intents.ACTION_PROFILE_REQUEST_UPDATE,
      )
    ) {
      ProfileReceiverStartAction.ScheduleUpdates -> {
        Global.launch {
          reset()

          val service =
            Intent(Intents.ACTION_PROFILE_SCHEDULE_UPDATES)
              .setComponent(ProfileWorker::class.componentName)

          context.startForegroundService(service)
        }
      }
      ProfileReceiverStartAction.RequestUpdate -> {
        val redirect = intent.setComponent(ProfileWorker::class.componentName)

        context.startForegroundService(redirect)
      }
      ProfileReceiverStartAction.Ignore -> Unit
    }
  }

  companion object {
    private val PROFILE_SCHEDULE_TRIGGER_ACTIONS =
      setOf(
        Intent.ACTION_BOOT_COMPLETED,
        Intent.ACTION_MY_PACKAGE_REPLACED,
        Intent.ACTION_TIMEZONE_CHANGED,
        Intent.ACTION_TIME_CHANGED,
      )

    private val lock = Mutex()
    private var initialized: Boolean = false

    suspend fun rescheduleAll(context: Context) = lock.withLock {
      if (initialized) return@withLock

      initialized = true

      Log.i("Reschedule all profiles update")

      ImportedDao()
        .queryAllUUIDs()
        .mapNotNull { ImportedDao().queryByUUID(it) }
        .filter { profileSupportsAutoUpdateSchedule(it.type) }
        .forEach { scheduleNext(context, it) }
    }

    fun cancelNext(context: Context, imported: Imported) {
      val intent = pendingIntentOf(context, imported)

      context.getSystemService<AlarmManager>()?.cancel(intent)
    }

    fun schedule(context: Context, imported: Imported) {
      val intent = pendingIntentOf(context, imported)

      context.getSystemService<AlarmManager>()?.cancel(intent)

      intent.send(context, 0, null)
    }

    fun scheduleNext(context: Context, imported: Imported) {
      val intent = pendingIntentOf(context, imported)

      context.getSystemService<AlarmManager>()?.cancel(intent)

      val current = System.currentTimeMillis()
      val last =
        context.importedDir.resolve(imported.uuid.toString()).resolve("config.yaml").lastModified()
      val delay =
        profileAutoUpdateScheduleDelayMillis(
          interval = imported.interval,
          currentTimeMillis = current,
          lastModifiedMillis = last,
        ) ?: return

      context.getSystemService<AlarmManager>()?.set(AlarmManager.RTC, current + delay, intent)
    }

    private suspend fun reset() = lock.withLock { initialized = false }

    private fun pendingIntentOf(context: Context, imported: Imported): PendingIntent {
      val intent =
        Intent(Intents.ACTION_PROFILE_REQUEST_UPDATE)
          .setComponent(ProfileReceiver::class.componentName)
          .setUUID(imported.uuid)

      return PendingIntent.getBroadcast(
        context,
        0,
        intent,
        pendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT),
      )
    }
  }
}
