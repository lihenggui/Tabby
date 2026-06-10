package com.github.kr328.clash.common.service

fun tabbyServiceNotificationProfileTitle(
  profileName: String?,
  defaultTitle: String,
): String {
  return profileName ?: defaultTitle
}
