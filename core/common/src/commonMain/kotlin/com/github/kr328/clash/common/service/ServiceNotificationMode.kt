package com.github.kr328.clash.common.service

enum class TabbyServiceNotificationMode {
  Dynamic,
  Static,
}

fun tabbyServiceNotificationMode(dynamicNotification: Boolean): TabbyServiceNotificationMode {
  return if (dynamicNotification) {
    TabbyServiceNotificationMode.Dynamic
  } else {
    TabbyServiceNotificationMode.Static
  }
}
