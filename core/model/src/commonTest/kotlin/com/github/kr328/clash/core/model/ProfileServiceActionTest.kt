package com.github.kr328.clash.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileServiceActionTest {
  @Test
  fun profileReceiverStartActionSchedulesUpdatesForSystemClockAndPackageEvents() {
    val scheduleTriggerActions =
      setOf(
        "android.intent.action.BOOT_COMPLETED",
        "android.intent.action.MY_PACKAGE_REPLACED",
        "android.intent.action.TIMEZONE_CHANGED",
        "android.intent.action.TIME_SET",
      )

    scheduleTriggerActions.forEach { action ->
      assertEquals(
        ProfileReceiverStartAction.ScheduleUpdates,
        profileReceiverStartAction(
          action = action,
          scheduleTriggerActions = scheduleTriggerActions,
          requestUpdateAction = "io.github.goooler.tabby.intent.action.PROFILE_REQUEST_UPDATE",
        ),
      )
    }
  }

  @Test
  fun profileReceiverStartActionRequestsUpdateForProfileUpdateAction() {
    assertEquals(
      ProfileReceiverStartAction.RequestUpdate,
      profileReceiverStartAction(
        action = "io.github.goooler.tabby.intent.action.PROFILE_REQUEST_UPDATE",
        scheduleTriggerActions = setOf("android.intent.action.BOOT_COMPLETED"),
        requestUpdateAction = "io.github.goooler.tabby.intent.action.PROFILE_REQUEST_UPDATE",
      ),
    )
  }

  @Test
  fun profileReceiverStartActionIgnoresMissingOrUnknownActions() {
    assertEquals(
      ProfileReceiverStartAction.Ignore,
      profileReceiverStartAction(
        action = null,
        scheduleTriggerActions = setOf("android.intent.action.BOOT_COMPLETED"),
        requestUpdateAction = "io.github.goooler.tabby.intent.action.PROFILE_REQUEST_UPDATE",
      ),
    )
    assertEquals(
      ProfileReceiverStartAction.Ignore,
      profileReceiverStartAction(
        action = "android.intent.action.PACKAGE_ADDED",
        scheduleTriggerActions = setOf("android.intent.action.BOOT_COMPLETED"),
        requestUpdateAction = "io.github.goooler.tabby.intent.action.PROFILE_REQUEST_UPDATE",
      ),
    )
  }

  @Test
  fun profileWorkerStartActionMapsRequestAndScheduleActions() {
    assertEquals(
      ProfileWorkerStartAction.RequestUpdate,
      profileWorkerStartAction(
        action = "io.github.goooler.tabby.intent.action.PROFILE_REQUEST_UPDATE",
        requestUpdateAction = "io.github.goooler.tabby.intent.action.PROFILE_REQUEST_UPDATE",
        scheduleUpdatesAction = "io.github.goooler.tabby.intent.action.SCHEDULE_UPDATES",
      ),
    )
    assertEquals(
      ProfileWorkerStartAction.ScheduleUpdates,
      profileWorkerStartAction(
        action = "io.github.goooler.tabby.intent.action.SCHEDULE_UPDATES",
        requestUpdateAction = "io.github.goooler.tabby.intent.action.PROFILE_REQUEST_UPDATE",
        scheduleUpdatesAction = "io.github.goooler.tabby.intent.action.SCHEDULE_UPDATES",
      ),
    )
  }

  @Test
  fun profileWorkerStartActionIgnoresMissingOrUnknownActions() {
    assertEquals(
      ProfileWorkerStartAction.Ignore,
      profileWorkerStartAction(
        action = null,
        requestUpdateAction = "io.github.goooler.tabby.intent.action.PROFILE_REQUEST_UPDATE",
        scheduleUpdatesAction = "io.github.goooler.tabby.intent.action.SCHEDULE_UPDATES",
      ),
    )
    assertEquals(
      ProfileWorkerStartAction.Ignore,
      profileWorkerStartAction(
        action = "io.github.goooler.tabby.intent.action.PROFILE_CHANGED",
        requestUpdateAction = "io.github.goooler.tabby.intent.action.PROFILE_REQUEST_UPDATE",
        scheduleUpdatesAction = "io.github.goooler.tabby.intent.action.SCHEDULE_UPDATES",
      ),
    )
  }
}
